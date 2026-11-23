package id.tuturium.app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.ComponentActivity
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.ClearCredentialStateRequest
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.CancellationException
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.IntentCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val model by lazy { ViewModelProvider(this)[TuturiumModel::class.java] }
    private var pendingGoogleEmail: String? = null
    private var signingIn = false
    private var lastAuthorization = 0L

    private val driveConsent = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            TuturiumCloud.message("Izin Google Drive dibatalkan. Data lokal tidak dihapus.")
            return@registerForActivityResult
        }
        try {
            val authorized = Identity.getAuthorizationClient(this)
                .getAuthorizationResultFromIntent(result.data)
            completeAuthorization(authorized.accessToken)
        } catch (error: Exception) {
            android.util.Log.e("TuturiumCloud", "Drive consent failed", error)
            TuturiumCloud.message("Gagal memperoleh izin Drive: ${error.javaClass.simpleName}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) handle(intent)
        setContent {
            val cloud by TuturiumCloud.state.collectAsStateWithLifecycle()
            if (cloud.connected) Tuturium(model)
            else TuturiumCloudLoginScreen(cloud, onSignIn = ::startGoogleSignIn)
        }
    }

    /** Credential Manager signs in the Google account; Drive consent is
     * deliberately requested as a separate, least-privilege authorization. */
    fun startGoogleSignIn() {
        if (signingIn) return
        signingIn = true
        TuturiumCloud.message("Membuka akun Google…")
        lifecycleScope.launch {
            try {
                val option = GetSignInWithGoogleOption.Builder(
                    BuildConfig.GOOGLE_WEB_CLIENT_ID).build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(option).build()
                val result = CredentialManager.create(this@MainActivity).getCredential(
                    context = this@MainActivity, request = request
                )
                val credential = result.credential
                require(credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    "Kredensial Google tidak dikenali."
                }
                val account = GoogleIdTokenCredential.createFrom(credential.data)
                require(account.id.isNotBlank()) { "Alamat akun Google kosong." }
                pendingGoogleEmail = account.id
                requestDriveAuthorization()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                android.util.Log.e("TuturiumCloud", "Google sign-in failed", error)
                TuturiumCloud.message("Google Login gagal: ${error.localizedMessage ?: error.javaClass.simpleName}")
            } finally {
                signingIn = false
            }
        }
    }

    private fun requestDriveAuthorization() {
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope("https://www.googleapis.com/auth/drive.appdata")))
            .build()
        Identity.getAuthorizationClient(this).authorize(request)
            .addOnSuccessListener { authorized ->
                if (authorized.hasResolution()) {
                    val intent = authorized.pendingIntent
                    if (intent == null) {
                        TuturiumCloud.message("Google tidak menyediakan dialog izin Drive.")
                    } else {
                        driveConsent.launch(IntentSenderRequest.Builder(intent.intentSender).build())
                    }
                } else {
                    completeAuthorization(authorized.accessToken)
                }
            }.addOnFailureListener { error ->
                android.util.Log.e("TuturiumCloud", "Drive authorization failed", error)
                TuturiumCloud.message("Izin Drive gagal: ${error.localizedMessage ?: "Coba lagi."}")
            }
    }

    private fun completeAuthorization(accessToken: String?) {
        if (accessToken.isNullOrBlank()) {
            TuturiumCloud.message("Token Google Drive kosong. Coba masuk ulang.")
            return
        }
        val account = pendingGoogleEmail ?: TuturiumCloud.state.value.email
        if (account.isNullOrBlank()) {
            TuturiumCloud.message("Akun Google tidak ditemukan. Coba masuk lagi.")
            return
        }
        lastAuthorization = System.currentTimeMillis()
        lifecycleScope.launch {
            try {
                TuturiumCloud.reconnectToken(applicationContext, account, accessToken)
                model.refresh()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                android.util.Log.e("TuturiumCloud", "Initial Drive sync failed", error)
                TuturiumCloud.message(
                    "Sinkronisasi belum berhasil: ${error.localizedMessage ?: error.javaClass.simpleName}. " +
                    "Data lama tetap aman di perangkat.")
            }
        }
    }

    fun syncGoogleDrive() = TuturiumCloud.uploadSoon(applicationContext)

    fun signOutGoogle() {
        lifecycleScope.launch {
            TuturiumCloud.syncNow(applicationContext)
            TuturiumCloud.disconnect()
            runCatching {
                CredentialManager.create(this@MainActivity)
                    .clearCredentialState(ClearCredentialStateRequest())
            }.onFailure { android.util.Log.w("TuturiumCloud", "Clear Google session failed", it) }
            pendingGoogleEmail = null
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handle(intent)
    }

    override fun onResume() {
        super.onResume()
        model.refresh()
        model.reloadVoicesIfOpened()
        model.returnFromInstallerSettings(this)
        if (TuturiumCloud.state.value.connected &&
            System.currentTimeMillis() - lastAuthorization > 45 * 60_000L) {
            pendingGoogleEmail = TuturiumCloud.state.value.email
            requestDriveAuthorization()
        }
    }

    override fun onPause() {
        if (TuturiumCloud.state.value.connected) TuturiumCloud.uploadSoon(applicationContext)
        super.onPause()
    }

    private fun handle(intent: Intent) {
        when (intent.action) {
            ReadingService.OPEN_PLAYER -> ReadingService.state.value.bookId?.let(model::open)
            Intent.ACTION_VIEW -> intent.data?.takeIf { it.scheme == "content" }?.let(model::import)
            Intent.ACTION_SEND -> {
                val file = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
                if (file?.scheme == "content") {
                    model.import(file)
                } else {
                    intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()?.let {
                        model.textDraft = it.take(DocumentImporter.MAX_TEXT)
                        model.showPaste = true
                    }
                }
            }
        }
    }
}

class TuturiumModel(private val app: Application) : AndroidViewModel(app) {
    private val library = Library.get(app)
    private val prefs = app.getSharedPreferences("settings", 0)
    private var openJob: Job? = null
    private var importJob: Job? = null
    private var checked = false
    private var waitingForInstallerPermission = false
    private var voiceProbe: TextToSpeech? = null
    private var voiceLoadToken = 0
    private var voiceReady = false
    private var previewToken = 0

    var books by mutableStateOf<List<Book>>(emptyList())
        private set
    var deletedBooks by mutableStateOf<List<Book>>(emptyList())
        private set
    var reader by mutableStateOf<Book?>(null)
        private set
    var content by mutableStateOf<ReadingDocument?>(null)
        private set
    var bookmarks by mutableStateOf<List<Bookmark>>(emptyList())
        private set
    var position by mutableStateOf(0)
        private set
    var status by mutableStateOf<String?>(null)
    var importing by mutableStateOf<String?>(null)
        private set
    var showPaste by mutableStateOf(false)
    var textDraft by mutableStateOf("")
    var settings by mutableStateOf(ReadingSettings.read(app))
        private set
    var listeningOptions by mutableStateOf(ListeningOptions.read(app))
        private set
    var voices by mutableStateOf<List<VoiceChoice>>(emptyList())
        private set
    var voiceStatus by mutableStateOf("Memuat pustaka suara…")
        private set
    var previewingVoice by mutableStateOf<String?>(null)
        private set
    var previewLoading by mutableStateOf(false)
        private set
    var previewError by mutableStateOf<String?>(null)
        private set
    var favoriteVoices by mutableStateOf(
        prefs.getStringSet("favorite_voices", emptySet()).orEmpty().toSet()
    )
        private set
    var theme by mutableStateOf(prefs.getString("theme", "dark") ?: "dark")
        private set
    var fontSize by mutableStateOf(prefs.getFloat("font_size", 22f))
        private set
    var follow by mutableStateOf(prefs.getBoolean("follow", true))
        private set

    var release by mutableStateOf<Release?>(null)
        private set
    var showUpdate by mutableStateOf(false)
    var updateStatus by mutableStateOf<String?>(null)
        private set
    var checking by mutableStateOf(false)
        private set
    var updateProgress by mutableStateOf<Float?>(null)
        private set
    var verifiedApk by mutableStateOf<File?>(null)
        private set

    fun refresh() = viewModelScope.launch {
        val (active, deleted) = withContext(Dispatchers.IO) {
            library.fillMissingWordCounts()
            library.books() to library.trashed()
        }
        books = active
        deletedBooks = deleted
    }

    fun open(id: String) {
        if (reader?.id == id && content != null) return
        openJob?.cancel()
        openJob = viewModelScope.launch {
            safe("Dokumen tidak dapat dibuka") {
                val (book, document, marks) = withContext(Dispatchers.IO) {
                    Triple(library.book(id) ?: error("Dokumen sudah dihapus."), library.document(id), library.bookmarks(id))
                }
                position = book.position.takeIf { it < book.count } ?: 0
                bookmarks = marks
                content = document
                reader = book
            }
        }
    }

    fun closeReader() {
        openJob?.cancel()
        reader = null
        content = null
        refresh()
    }

    fun import(uri: Uri) = addDocument {
        DocumentImporter(app).import(uri) { importing = it }
    }

    fun addText(title: String, text: String) = addDocument {
        require(text.length <= DocumentImporter.MAX_TEXT) { "Teks terlalu panjang." }
        val heading = title.trim().ifBlank { "Teks pribadi" }
        library.add(heading, "TXT", ReadingDocument(listOf(Section(heading, text.trim()
            .replace("\r\n", "\n").replace('\r', '\n')))))
    }

    private fun addDocument(insert: suspend () -> Book) {
        if (importJob?.isActive == true) return
        importing = "Membuka dokumen…"
        importJob = viewModelScope.launch {
            try {
                safe("Impor gagal") {
                    val book = withContext(Dispatchers.IO) { insert() }
                    refresh()
                    open(book.id)
                }
            } finally {
                importing = null
            }
        }
    }

    fun cancelImport() = importJob?.cancel()

    fun importLink(url: String) = addDocument {
        val parsed = java.net.URI(url.trim())
        require(parsed.scheme == "https" && parsed.host != null &&
            parsed.host.matches(Regex("[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+"))) {
            "Gunakan tautan HTTPS ke halaman web publik."
        }
        val page = org.jsoup.Jsoup.connect(parsed.toString()).userAgent("Tuturium/1.1")
            .timeout(15_000).maxBodySize(2_000_000).followRedirects(false).get()
        val title = page.title().ifBlank { parsed.host }.take(200)
        val text = page.body().text().take(DocumentImporter.MAX_TEXT)
        library.add(title, "WEB", ReadingDocument(listOf(Section(title, text))))
    }

    fun rename(book: Book, title: String) = viewModelScope.launch {
        safe("Judul gagal disimpan") {
            withContext(Dispatchers.IO) { library.rename(book.id, title) }
            if (reader?.id == book.id) reader = book.copy(title = title.trim())
            refresh()
        }
    }

    fun remove(book: Book) = viewModelScope.launch {
        safe("Dokumen gagal dihapus") {
            if (ReadingService.state.value.bookId == book.id) ReadingService.command(app, ReadingService.STOP)
            if (reader?.id == book.id) closeReader()
            withContext(Dispatchers.IO) { library.delete(book.id) }
            BookActions.remove(app, book.id)
            refresh()
        }
    }

    fun shareBook(book: Book) = viewModelScope.launch {
        safe("Gagal membagikan dokumen") {
            // Export a readable text copy, never expose the private database file.
            val export = withContext(Dispatchers.IO) {
                val doc = library.document(book.id)
                val dir = File(app.cacheDir, "shared").apply { mkdirs() }
                File(dir, "Tuturium-${book.id}.txt").apply {
                    writeText(doc.sections.joinToString("\n\n") { it.text }, Charsets.UTF_8)
                }
            }
            val uri = FileProvider.getUriForFile(app, "${app.packageName}.updates", export)
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, book.title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            app.startActivity(Intent.createChooser(send, "Bagikan bacaan").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    fun removeMany(books: List<Book>) = viewModelScope.launch {
        safe("Dokumen gagal dihapus") {
            val ids = books.map { it.id }.toSet()
            if (ReadingService.state.value.bookId in ids)
                ReadingService.command(app, ReadingService.STOP)
            if (reader?.id in ids) closeReader()
            withContext(Dispatchers.IO) { ids.forEach(library::delete) }
            ids.forEach { BookActions.remove(app, it) }
            refresh()
        }
    }

    fun restore(book: Book) = viewModelScope.launch {
        safe("Pemulihan gagal") {
            withContext(Dispatchers.IO) { library.restore(book.id) }
            refresh()
        }
    }

    fun deletePermanently(book: Book) = viewModelScope.launch {
        safe("Penghapusan gagal") {
            withContext(Dispatchers.IO) { library.deletePermanently(book.id) }
            refresh()
        }
    }

    fun toggleBookmark(index: Int) = viewModelScope.launch {
        val book = reader ?: return@launch
        val pass = content?.passes?.getOrNull(index) ?: return@launch
        safe("Markah gagal disimpan") {
            val marks = withContext(Dispatchers.IO) {
                library.toggleBookmark(book.id, index, pass.text)
                library.bookmarks(book.id)
            }
            if (reader?.id == book.id) bookmarks = marks
        }
    }

    fun seek(index: Int) {
        val book = reader ?: return
        val next = index.coerceIn(0, book.count - 1)
        if (ReadingService.state.value.bookId == book.id) {
            ReadingService.command(app, ReadingService.SEEK, index = next)
        } else {
            position = next
            viewModelScope.launch {
                safe("Posisi gagal disimpan") { withContext(databaseWrites) { library.position(book.id, next) } }
            }
        }
    }

    /** Tap-to-read always speaks from the selected word, even inside the current sentence. */
    fun playFromWord(book: Book, selection: WordSelection) {
        if (reader?.id != book.id) return
        val pass = content?.passes?.getOrNull(selection.passIndex) ?: return
        val offset = selection.offset.coerceIn(0, pass.text.lastIndex.coerceAtLeast(0))
        val started = ReadingService.command(app, ReadingService.PLAY,
            book = book.id, index = selection.passIndex, offset = offset)
        if (!started) status = "Pemutar Android tidak dapat dimulai. Coba lagi."
    }

    fun play(book: Book, index: Int = -1) {
        val state = ReadingService.state.value
        val launched = if (state.bookId == book.id && (state.playing || state.loading))
            ReadingService.command(app, ReadingService.PAUSE)
        else ReadingService.command(app, ReadingService.PLAY, book.id, index)
        if (!launched) status = "Pemutar Android tidak dapat dimulai. Buka Tuturium dan coba lagi."
    }

    fun reloadVoicesIfOpened() {
        if (voiceProbe != null) loadVoices()
    }

    fun loadVoices() {
        val token = ++voiceLoadToken
        stopVoicePreview()
        voiceReady = false
        voiceProbe?.shutdown()
        voiceProbe = null
        voiceStatus = "Memuat suara Google Indonesia…"
        // Always select the Google engine explicitly, never the device default.
        try {
            voiceProbe = TextToSpeech(app, { result ->
                viewModelScope.launch(Dispatchers.Main) {
                    if (token != voiceLoadToken) return@launch
                    val engine = voiceProbe
                    val googleReady = result == TextToSpeech.SUCCESS &&
                        runCatching { engine?.engines.orEmpty().any {
                            it.name == GOOGLE_TTS_ENGINE
                        } }.getOrDefault(false)
                    voiceReady = googleReady
                    voices = if (googleReady && engine != null)
                        engine.indonesianCandidates() else emptyList()
                    if (googleReady) engine?.setOnUtteranceProgressListener(previewListener)
                    voiceStatus = when {
                        !googleReady -> "Google Speech Services belum tersedia."
                        voices.isEmpty() ->
                            "Google TTS tidak melaporkan suara bahasa Indonesia. Periksa jaringan atau pasang data suara."
                        else -> ""
                    }
                }
            }, GOOGLE_TTS_ENGINE)
        } catch (error: Exception) {
            android.util.Log.e("TuturiumVoice", "Google TTS cannot initialize", error)
            voices = emptyList()
            voiceReady = false
            voiceStatus = "Google Speech Services tidak dapat dijalankan."
        }
    }

    private val previewListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            updatePreview(utteranceId) { previewLoading = false }
        }
        override fun onDone(utteranceId: String?) {
            updatePreview(utteranceId) {
                previewingVoice = null
                previewLoading = false
            }
        }
        @Deprecated("Required on older Android")
        override fun onError(utteranceId: String?) {
            previewFailure(utteranceId, TextToSpeech.ERROR)
        }
        override fun onError(utteranceId: String?, errorCode: Int) {
            previewFailure(utteranceId, errorCode)
        }
        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            updatePreview(utteranceId) {
                previewingVoice = null
                previewLoading = false
            }
        }
    }

    private fun updatePreview(id: String?, action: () -> Unit) {
        if (id != "preview-$previewToken") return
        viewModelScope.launch(Dispatchers.Main.immediate) {
            if (id == "preview-$previewToken") action()
        }
    }

    private fun previewFailure(id: String?, error: Int) {
        updatePreview(id) {
            previewingVoice = null
            previewLoading = false
            previewError = when (error) {
                TextToSpeech.ERROR_NETWORK, TextToSpeech.ERROR_NETWORK_TIMEOUT ->
                    "Pratinjau gagal: jaringan tidak tersedia."
                TextToSpeech.ERROR_NOT_INSTALLED_YET ->
                    "Paket suara ini belum siap. Unduh data suara Google atau coba suara lain."
                else -> "Pratinjau suara gagal. Coba suara Indonesia lainnya."
            }
        }
    }

    fun stopVoicePreview() {
        ++previewToken
        previewingVoice = null
        previewLoading = false
        voiceProbe?.stop()
    }

    fun previewVoice(name: String) {
        if (previewingVoice == name) {
            stopVoicePreview()
            return
        }
        stopVoicePreview()
        previewError = null
        val choice = voices.firstOrNull { it.name == name && isIndonesian(it.language) }
        val engine = voiceProbe
        if (choice == null || !voiceReady || engine == null) {
            previewError = "Suara belum siap. Muat ulang pustaka suara."
            return
        }
        if (!engine.activateIndonesianVoice(choice)) {
            previewError = "Suara Indonesia tidak tersedia. Periksa koneksi atau paket Google TTS."
            return
        }
        // Avoid mixing narrator audio with the selected voice's preview.
        if (ReadingService.state.value.playing || ReadingService.state.value.loading) {
            ReadingService.command(app, ReadingService.PAUSE)
        }
        val utteranceId = "preview-${++previewToken}"
        previewingVoice = name
        previewLoading = true
        runCatching {
            engine.setSpeechRate(1f)
            engine.setPitch(1f)
            engine.speak(
                "Halo, ini pratinjau suara Indonesia dari Google. " +
                    "Anda dapat memilih suara ini untuk mendengarkan buku di Tuturium.",
                TextToSpeech.QUEUE_FLUSH, null, utteranceId
            )
        }.onSuccess { result ->
            if (result == TextToSpeech.ERROR) previewFailure(utteranceId, TextToSpeech.ERROR)
        }.onFailure {
            android.util.Log.e("TuturiumVoice", "Preview failed", it)
            previewFailure(utteranceId, TextToSpeech.ERROR)
        }
    }

    fun toggleFavoriteVoice(name: String) {
        if (voices.none { it.name == name }) return
        favoriteVoices = if (name in favoriteVoices) favoriteVoices - name
            else favoriteVoices + name
        prefs.edit().putStringSet("favorite_voices", favoriteVoices.toSet()).apply()
    }

    fun installGoogleVoices() {
        val intent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).apply {
            setPackage(GOOGLE_TTS_ENGINE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { app.startActivity(intent) }
            .onFailure {
                runCatching {
                    app.startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$GOOGLE_TTS_ENGINE"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }.onFailure {
                    status = "Google TTS belum tersedia di perangkat ini."
                }
            }
    }

    fun changeSettings(value: ReadingSettings, persist: Boolean = true) {
        settings = value.copy(speed = value.speed.coerceIn(0.5f, 4f))
        if (persist) settings.save(app)
    }

    fun changeListeningOptions(value: ListeningOptions) {
        listeningOptions = value
        value.save(app)
        if (follow != value.autoScroll) changeFollow(value.autoScroll)
    }

    fun changeTheme(value: String) {
        theme = value
        prefs.edit().putString("theme", value).apply()
    }

    fun changeFont(value: Float) {
        fontSize = value
        prefs.edit().putFloat("font_size", value).apply()
    }

    fun changeFollow(value: Boolean) {
        follow = value
        prefs.edit().putBoolean("follow", value).apply()
    }

    fun checkUpdate(automatic: Boolean = false) {
        if (checking || updateProgress != null || (automatic && checked)) return
        checked = true
        checking = true
        if (!automatic) updateStatus = "Memeriksa GitHub…"
        viewModelScope.launch {
            try {
                val latest = Updates.latest()
                if (latest != release) verifiedApk = null
                release = latest
                updateStatus = if (latest == null) "Versi terbaru sudah terpasang." else "Pembaruan tersedia."
                if (latest != null) showUpdate = true
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                if (!automatic) updateStatus = "Pemeriksaan gagal. Periksa koneksi dan coba lagi."
            } finally {
                checking = false
            }
        }
    }

    fun downloadUpdate(context: Context) {
        if (updateProgress != null) return
        val latest = release ?: return
        viewModelScope.launch {
            try {
                val apk = verifiedApk?.takeIf { it.isFile } ?: run {
                    updateProgress = 0f
                    updateStatus = "Mengunduh APK…"
                    Updates.download(app, latest) { updateProgress = it }.also { verifiedApk = it }
                }
                updateStatus = "APK terverifikasi dan siap dipasang."
                waitingForInstallerPermission = true
                if (Updates.install(context, apk)) waitingForInstallerPermission = false
                else updateStatus = "Izinkan Tuturium memasang pembaruan di setelan Android."
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                waitingForInstallerPermission = false
                updateStatus = error.message ?: "Pembaruan gagal."
            } finally {
                updateProgress = null
            }
        }
    }

    fun returnFromInstallerSettings(context: Context) {
        if (!waitingForInstallerPermission) return
        waitingForInstallerPermission = false
        if (context.packageManager.canRequestPackageInstalls()) downloadUpdate(context)
    }

    private suspend fun safe(message: String, operation: suspend () -> Unit) {
        try {
            operation()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            status = "$message. ${error.message.orEmpty()}".trim()
        }
    }

    override fun onCleared() {
        stopVoicePreview()
        voiceProbe?.shutdown()
        super.onCleared()
    }

    companion object {
        private val databaseWrites = Dispatchers.IO.limitedParallelism(1)
    }
}
