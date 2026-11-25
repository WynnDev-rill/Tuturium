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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.coroutines.tasks.await
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.gms.auth.api.identity.Identity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.IntentCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val model by lazy { ViewModelProvider(this)[TuturiumModel::class.java] }
    private var pendingDriveEmail: String? = null
    private var accountOperation: Job? = null
    private var driveCheckedAt = -1L

    private val driveConsent = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val email = pendingDriveEmail
        pendingDriveEmail = null
        if (email == null) {
            TuturiumCloud.driveFailure("Sesi izin berakhir. Buka status Drive untuk mencoba lagi.")
        } else if (result.resultCode != Activity.RESULT_OK) {
            TuturiumCloud.driveFailure("Izin Drive dibatalkan. Buka status Drive jika ingin mencoba lagi.")
        } else {
            runAccountOperation(drive = true) {
                val authorized = Identity.getAuthorizationClient(this)
                    .getAuthorizationResultFromIntent(result.data)
                completeAuthorization(email, authorized.accessToken, authorized.grantedScopes)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingDriveEmail = savedInstanceState?.getString("pending_drive_email")
        TuturiumCloud.restoreIdentity(applicationContext)
        if (savedInstanceState == null) handle(intent)
        setContent { Tuturium(model) }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    restoreDriveConnection()
                    delay(60_000L)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("pending_drive_email", pendingDriveEmail)
        super.onSaveInstanceState(outState)
    }

    fun startGoogleSignIn() = runAccountOperation(drive = false) {
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            TuturiumCloud.message("Google Play Services belum siap. Aktifkan atau perbarui melalui Play Store.")
            return@runAccountOperation
        }
        TuturiumCloud.waiting("Membuka akun Google…")
        val manager = CredentialManager.create(this)
        val webId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        val credential = try {
            val button = GetSignInWithGoogleOption.Builder(webId).build()
            manager.getCredential(this, GetCredentialRequest.Builder().addCredentialOption(button).build()).credential
        } catch (error: GetCredentialException) {
            if (error !is NoCredentialException && !isAccountReauthFailure(error.message)) throw error
            val chooser = GetGoogleIdOption.Builder().setServerClientId(webId)
                .setFilterByAuthorizedAccounts(false).setAutoSelectEnabled(false).build()
            try {
                manager.getCredential(this, GetCredentialRequest.Builder().addCredentialOption(chooser).build()).credential
            } catch (fallback: GetCredentialException) {
                if (fallback is NoCredentialException && isAccountReauthFailure(error.message)) throw error
                throw fallback
            }
        }
        require(credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)
        val account = GoogleIdTokenCredential.createFrom(credential.data)
        TuturiumCloud.signIn(applicationContext, account.id, account.displayName, account.profilePictureUri?.toString())
        authorizeDrive(account.id, allowConsent = true)
    }

    fun retryGoogleDrive() = runAccountOperation(drive = true) {
        val email = TuturiumCloud.state.value.email ?: return@runAccountOperation
        authorizeDrive(email, allowConsent = true)
    }

    private suspend fun authorizeDrive(email: String, allowConsent: Boolean) {
        driveCheckedAt = android.os.SystemClock.elapsedRealtime()
        val client = Identity.getAuthorizationClient(this)
        TuturiumCloud.waiting("Memeriksa izin Google Drive…")
        val authorized = client.authorize(driveAuthorization(email)).await()
        if (authorized.hasResolution()) {
            if (!allowConsent) {
                TuturiumCloud.driveFailure("Google Drive memerlukan izin. Buka status Drive untuk melanjutkan.")
                return
            }
            TuturiumCloud.rememberDriveConsentAttempt(applicationContext)
            val intent = authorized.pendingIntent ?: error("Dialog izin Drive tidak tersedia.")
            pendingDriveEmail = email
            try {
                driveConsent.launch(IntentSenderRequest.Builder(intent.intentSender).build())
            } catch (error: Exception) {
                pendingDriveEmail = null
                throw error
            }
        } else {
            completeAuthorization(email, authorized.accessToken, authorized.grantedScopes)
        }
    }

    private suspend fun completeAuthorization(email: String, token: String?, scopes: List<String>) {
        check(TuturiumCloud.state.value.email == email) { "Sesi akun berubah. Coba lagi." }
        if (token.isNullOrBlank() || DRIVE_APPDATA_SCOPE !in scopes) {
            TuturiumCloud.driveFailure("Izin penyimpanan Drive belum diberikan.")
            return
        }
        TuturiumCloud.connect(applicationContext, email, token)
        model.reloadCloudPreferences()
        model.refresh()
    }

    private fun runAccountOperation(drive: Boolean, block: suspend () -> Unit) {
        if (accountOperation?.isActive == true || pendingDriveEmail != null) return
        accountOperation = lifecycleScope.launch {
            try {
                block()
            } catch (error: CancellationException) {
                if (pendingDriveEmail == null) {
                    if (TuturiumCloud.state.value.email != null) TuturiumCloud.driveFailure("Pemeriksaan Google terhenti.")
                    else TuturiumCloud.message("Operasi Google terhenti. Coba lagi.")
                }
                throw error
            } catch (error: Exception) {
                android.util.Log.w("TuturiumCloud", "Google operation failed: ${error.javaClass.simpleName}")
                val driveOperation = drive || TuturiumCloud.state.value.email != null
                val message = if (error is CloudAccessException) error.message.orEmpty()
                    else googleAccessMessage(error, driveOperation)
                if (driveOperation) TuturiumCloud.driveFailure(message) else TuturiumCloud.message(message)
            }
        }
    }

    fun signOutGoogle() = runAccountOperation(drive = false) {
        TuturiumCloud.waiting("Mengakhiri sesi Google…")
        TuturiumCloud.disconnect(applicationContext)
        runCatching {
            CredentialManager.create(this@MainActivity).clearCredentialState(ClearCredentialStateRequest())
        }.onFailure { android.util.Log.w("TuturiumCloud", "Clear credential state: ${it.javaClass.simpleName}") }
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
        model.checkUpdate(automatic = true)
    }

    private fun restoreDriveConnection() {
        val email = TuturiumCloud.state.value.email ?: return
        val now = android.os.SystemClock.elapsedRealtime()
        if (TuturiumCloud.shouldReconnect && (driveCheckedAt < 0 || now - driveCheckedAt >= 30_000L)) {
            runAccountOperation(drive = true) {
                authorizeDrive(email, allowConsent = !TuturiumCloud.hasAttemptedDriveConsent(applicationContext))
            }
        }
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
    private val timelineDispatcher = Dispatchers.IO.limitedParallelism(1)
    private val timelines = object : android.util.LruCache<Triple<String, Long, ListeningOptions>, ReadingTimeline>(
        8 * 1024 * 1024
    ) {
        override fun sizeOf(key: Triple<String, Long, ListeningOptions>, value: ReadingTimeline) = value.sizeBytes
    }

    internal suspend fun readingTimeline(book: Book, document: ReadingDocument?, options: ListeningOptions) =
        withContext(timelineDispatcher) {
            try {
                val key = Triple(book.id, library.documentModifiedAt(book.id), options)
                timelines.get(key) ?: ReadingTimeline(document ?: library.narrationDocument(book.id, options.skipEnabled), options)
                    .also { timelines.put(key, it) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                android.util.Log.w("Tuturium", "Reading time index could not be calculated", error)
                null
            }
        }
    private val prefs = app.getSharedPreferences("settings", 0)
    private var openJob: Job? = null
    private var importJob: Job? = null
    private var nextAutomaticUpdateCheck = 0L
    private var waitingForInstallerPermission = false
    private var voiceProbe: TextToSpeech? = null
    private var voiceLoadToken = 0
    private var voiceReady = false
    private var previewToken = 0

    var books by mutableStateOf<List<Book>>(emptyList())
        private set
    var deletedBooks by mutableStateOf<List<Book>>(emptyList())
        private set
    internal var readerPace by mutableStateOf(newReaderPace())
        private set

    private fun newReaderPace() = NarrationPaceSnapshot { ReadingPace.millisPerWord(app, it) }

    var reader by mutableStateOf<Book?>(null)
        private set
    var content by mutableStateOf<ReadingDocument?>(null, androidx.compose.runtime.referentialEqualityPolicy())
        private set
    internal var pdfLayoutRevision by mutableIntStateOf(0)
        private set
    private var narrationLayoutJob: Job? = null
    var bookmarks by mutableStateOf<List<Bookmark>>(emptyList())
        private set
    var savingBookId by mutableStateOf<String?>(null)
        private set
    var savingReader by mutableStateOf(false)
        private set
    var position by mutableStateOf(0)
        private set
    var positionOffset by mutableStateOf(0)
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
    private val offlinePreview by lazy { OfflineAudio(app) }
    var installedModels by mutableStateOf<Set<String>>(emptySet())
        private set
    var acceleratedSupertonic by mutableStateOf(false)
        private set
    var downloadingModel by mutableStateOf<String?>(null)
        private set
    var modelDownloadProgress by mutableStateOf<Float?>(null)
        private set
    var failedModelId by mutableStateOf<String?>(null)
        private set
    var modelDownloadError by mutableStateOf<String?>(null)
        private set
    private var modelDownloadJob: Job? = null

    init { refreshOfflineModels() }

    fun refreshOfflineModels() {
        viewModelScope.launch {
            val (onDisk, accelerated) = withContext(Dispatchers.IO) {
                val installed = OfflineCatalog.models.filter { OfflineModelStore.installed(app, it) }
                installed.map { it.id }.toSet() to installed.any { OfflineModelStore.acceleratedVector(app, it) != null }
            }
            installedModels = installedModels + onDisk
            acceleratedSupertonic = acceleratedSupertonic || accelerated
        }
    }

    fun downloadVoiceModel(id: String, acceleration: Boolean = false) {
        if (downloadingModel != null) return
        val model = OfflineCatalog.model(id)
        downloadingModel = id
        modelDownloadProgress = 0f
        modelDownloadError = null
        failedModelId = null
        modelDownloadJob = viewModelScope.launch {
            try {
                if (acceleration) {
                    OfflineModelStore.installAcceleration(app, model) { modelDownloadProgress = it }
                    acceleratedSupertonic = true
                } else {
                    OfflineModelStore.install(app, model) { modelDownloadProgress = it }
                    installedModels = installedModels + id
                }
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) {
                failedModelId = id
                modelDownloadError = "Unduhan gagal: " + error.message.orEmpty()
            } finally { downloadingModel = null; modelDownloadProgress = null }
        }
    }

    fun cancelVoiceDownload() { modelDownloadJob?.cancel() }

    private fun previewOfflineVoice(voice: OfflineVoice) {
        if (voice.model !in installedModels) {
            previewError = "Unduh Supertonic untuk mendengarkan pratinjau."
            return
        }
        if (ReadingService.state.value.playing || ReadingService.state.value.loading)
            ReadingService.command(app, ReadingService.PAUSE)
        previewingVoice = voice.id
        previewLoading = true
        val text = "Halo, ini pratinjau suara bahasa Indonesia. Anda dapat mendengarkan buku secara offline di Tuturium."
        val speech = PreparedSpeech(text, IntArray(text.length) { it }).normalizedForNarration()
        offlinePreview.play(viewModelScope, voice, sequenceOf(OfflinePass(0, speech)), 1f, 1f,
            onPass = {}, onSpoken = { _, _ -> }, onRange = {},
            onRendering = { previewLoading = !it },
            onDone = { previewingVoice = null; previewLoading = false },
            onError = {
                previewError = "Pratinjau Supertonic gagal: " + it.message.orEmpty()
                previewingVoice = null
                previewLoading = false
            })
    }

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
    var readerFont by mutableStateOf(prefs.getString("reader_font", "sans") ?: "sans")
        private set
    var readerHighlight by mutableStateOf(prefs.getString("reader_highlight", "theme") ?: "theme")
        private set
    var readerPreset by mutableStateOf(prefs.getString("reader_preset", "standard") ?: "standard")
        private set
    var readerHighlightSentence by mutableStateOf(prefs.getBoolean("reader_highlight_sentence", true))
        private set
    var readerBold by mutableStateOf(prefs.getBoolean("reader_bold", false))
        private set

    var fontSize by mutableStateOf(prefs.getFloat("font_size", 22f))
        private set
    var follow by mutableStateOf(prefs.getBoolean("follow", listeningOptions.autoScroll))
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

    /** Refresh Compose settings after importing a snapshot from another device. */
    fun reloadCloudPreferences() {
        readerFont = prefs.getString("reader_font", "sans") ?: "sans"
        readerHighlight = prefs.getString("reader_highlight", "theme") ?: "theme"
        readerPreset = prefs.getString("reader_preset", "standard") ?: "standard"
        readerHighlightSentence = prefs.getBoolean("reader_highlight_sentence", true)
        readerBold = prefs.getBoolean("reader_bold", false)
        settings = ReadingSettings.read(app)
        listeningOptions = ListeningOptions.read(app)
        favoriteVoices = prefs.getStringSet("favorite_voices", emptySet()).orEmpty().toSet()
        theme = prefs.getString("theme", "dark") ?: "dark"
        fontSize = prefs.getFloat("font_size", 22f)
        follow = prefs.getBoolean("follow", listeningOptions.autoScroll)
    }

    private var refreshJob: Job? = null
    private var wordCountsChecked = false

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            safe("Pustaka tidak dapat diperbarui") {
                val backfill = !wordCountsChecked
                val (active, deleted) = withContext(Dispatchers.IO) {
                    if (backfill) library.fillMissingWordCounts()
                    library.books() to library.trashed()
                }
                wordCountsChecked = true
                books = active
                deletedBooks = deleted
            }
        }
    }

    fun open(id: String) {
        if (savingBookId != null) return
        if (reader?.id == id && content != null) return
        openJob?.cancel()
        openJob = viewModelScope.launch {
            safe("Dokumen tidak dapat dibuka") {
                val (book, document, marks) = withContext(Dispatchers.IO) {
                    val book = library.book(id) ?: error("Dokumen sudah dihapus.")
                    val document = library.narrationDocument(id, listeningOptions.skipEnabled)
                    withContext(Dispatchers.Default) { document.readerLayout }
                    Triple(book, document, library.bookmarks(id))
                }
                position = book.position
                positionOffset = book.wordOffset
                bookmarks = marks
                content = document
                readerPace = newReaderPace()
                reader = book
            }
        }
    }

    fun closeReader() {
        if (savingReader) return
        openJob?.cancel()
        val book = reader ?: return
        savingReader = true
        savingBookId = book.id
        val previousDocument = content
        viewModelScope.launch {
            try {
                val state = ReadingService.state.value
                if (state.bookId == book.id && (state.playing || state.loading)) {
                    check(ReadingService.command(app, ReadingService.PAUSE)) { "Pemutar tidak dapat dijeda." }
                    kotlinx.coroutines.withTimeout(5000L) {
                        ReadingService.state.first { it.bookId != book.id || (!it.playing && !it.loading) }
                    }
                }
                reader = null
                content = null
                // The service queues its checkpoint before reporting paused.
                // Read after every pending write; never overwrite a newer service checkpoint.
                val saved = withContext(ReadingService.positionDispatcher) {
                    val latest = library.book(book.id) ?: error("Dokumen tidak ditemukan.")
                    val stopped = ReadingService.state.value
                    if (stopped.bookId == book.id && stopped.total > 0) {
                        val index = if (stopped.completed) latest.count else stopped.index
                        val offset = if (stopped.completed) 0 else stopped.offset
                        library.position(book.id, index, offset)
                    }
                    library.books()
                }
                books = saved
                if (ReadingService.state.value.bookId == book.id) ReadingService.command(app, ReadingService.STOP)
                // Keep the row indicator visible across the reader crossfade.
                kotlinx.coroutines.delay(450L)
            } catch (error: kotlinx.coroutines.TimeoutCancellationException) {
                content = previousDocument
                reader = book
                status = "Progres belum berhasil disimpan. Coba kembali lagi."
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                content = previousDocument
                reader = book
                status = "Progres belum berhasil disimpan. Coba kembali lagi."
            } finally {
                savingReader = false
                savingBookId = null
            }
        }
    }

    fun import(uri: Uri) = addDocument {
        DocumentImporter(app).import(uri) { importing = it }
    }

    fun addText(title: String, text: String, format: String = "TXT") = addDocument {
        require(text.length <= DocumentImporter.MAX_TEXT) { "Teks terlalu panjang." }
        val heading = title.trim().ifBlank { "Teks pribadi" }
        library.add(heading, format, ReadingDocument(listOf(Section(heading, text.trim()
            .replace("\r\n", "\n").replace('\r', '\n')))))
    }

    var importTargetFolder: String? = null

    private fun addDocument(insert: suspend () -> Book) {
        if (importJob?.isActive == true) return
        val targetFolder = importTargetFolder
        importTargetFolder = null
        importing = "Membuka dokumen…"
        importJob = viewModelScope.launch {
            try {
                safe("Impor gagal") {
                    val book = withContext(Dispatchers.IO) {
                        insert().also { if (targetFolder != null) FolderStore.assign(app, it.id, targetFolder) }
                    }
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

    fun hasOriginalPdf(book: Book): Boolean = book.format == "PDF" && library.originalPdf(book.id) != null

    fun exportBook(book: Book, destination: Uri, pdf: Boolean) = viewModelScope.launch {
        safe("Unduh dokumen gagal") {
            withContext(Dispatchers.IO) {
                val source = if (pdf) checkNotNull(library.originalPdf(book.id)) { "PDF asli tidak tersedia." } else null
                val text = if (source == null) library.document(book.id).sections.joinToString("\n\n") { it.text } else null
                val output = checkNotNull(app.contentResolver.openOutputStream(destination, "wt")) { "Lokasi penyimpanan tidak tersedia." }
                output.use { stream ->
                    if (source != null) source.inputStream().use { it.copyTo(stream) }
                    else stream.write(checkNotNull(text).toByteArray(Charsets.UTF_8))
                }
            }
            status = "Dokumen berhasil diunduh."
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

    var trashBusy by mutableStateOf(false)
        private set

    fun changeTrash(ids: Set<String>, permanent: Boolean) {
        if (trashBusy || ids.isEmpty()) return
        trashBusy = true
        viewModelScope.launch {
            try {
                safe(if (permanent) "Penghapusan gagal" else "Pemulihan gagal") {
                    withContext(Dispatchers.IO) {
                        val valid = library.trashed().filter { it.id in ids }
                        valid.forEach {
                            if (permanent) library.deletePermanently(it.id) else library.restore(it.id)
                        }
                    }
                }
                refresh()
            } finally {
                trashBusy = false
            }
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
            positionOffset = 0
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
        offlinePreview.stop()
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
        val offline = OfflineCatalog.voice(name)
        if (offline != null) {
            previewOfflineVoice(offline)
            return
        }
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
        if (OfflineCatalog.voice(name) == null && voices.none { it.name == name }) return
        favoriteVoices = if (name in favoriteVoices) favoriteVoices - name
            else favoriteVoices + name
        prefs.edit().putStringSet("favorite_voices", favoriteVoices.toSet()).apply()
        TuturiumCloud.markDirty(app)
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
        if (persist) {
            settings.save(app)
            TuturiumCloud.markDirty(app)
        }
    }

    internal fun choosePlaybackSpeed(speed: Float) {
        if (settings.speed == speed && listeningOptions.autoRamp && ReadingService.state.value.bookId != null) {
            ReadingService.command(app, ReadingService.RESET_SPEED_RAMP)
        } else {
            changeSettings(settings.copy(speed = speed))
        }
    }

    internal fun pdfMargins(book: Book): PdfMarginSettings = PdfMarginStore.read(app, book.id)

    internal fun savePdfMargins(book: Book, margins: PdfMarginSettings) {
        PdfMarginStore.write(app, book.id, margins)
        refreshPdfNarration(book)
    }

    private fun refreshPdfNarration(book: Book) {
        narrationLayoutJob?.cancel()
        val detect = listeningOptions.skipEnabled
        narrationLayoutJob = viewModelScope.launch {
            safe("Area bacaan PDF gagal diperbarui") {
                val refreshed = withContext(Dispatchers.IO) { library.narrationDocument(book.id, detect) }
                withContext(Dispatchers.Default) { refreshed.readerLayout }
                if (reader?.id == book.id) {
                    content = refreshed
                    timelines.evictAll()
                    pdfLayoutRevision++
                }
                if (ReadingService.state.value.bookId == book.id)
                    ReadingService.command(app, ReadingService.RELOAD_LAYOUT, book.id)
            }
        }
    }

    fun changeListeningOptions(value: ListeningOptions) {
        val previous = listeningOptions
        listeningOptions = value
        value.save(app)
        follow = value.autoScroll
        prefs.edit().putBoolean("follow", follow).apply()
        val pdf = reader?.takeIf { it.format == "PDF" && previous.skipEnabled != value.skipEnabled }
        if (pdf != null) refreshPdfNarration(pdf)
        else if (ReadingService.state.value.bookId != null &&
            previous.copy(autoplay = value.autoplay, hideAutoplay = value.hideAutoplay,
                autoScroll = value.autoScroll) != value
        ) ReadingService.command(app, ReadingService.SETTINGS)
        TuturiumCloud.markDirty(app)
    }

    fun changeTheme(value: String) {
        theme = value
        prefs.edit().putString("theme", value).apply()
        TuturiumCloud.markDirty(app)
    }

    fun changeReaderFont(value: String) {
        readerFont = value
        prefs.edit().putString("reader_font", value).apply()
        TuturiumCloud.markDirty(app)
    }

    fun changeReaderHighlight(value: String) {
        readerHighlight = value
        prefs.edit().putString("reader_highlight", value).apply()
        TuturiumCloud.markDirty(app)
    }

    fun changeReaderPreset(value: String) {
        readerPreset = value
        prefs.edit().putString("reader_preset", value).apply()
        TuturiumCloud.markDirty(app)
    }

    fun changeReaderHighlightSentence(value: Boolean) {
        readerHighlightSentence = value
        prefs.edit().putBoolean("reader_highlight_sentence", value).apply()
        TuturiumCloud.markDirty(app)
    }

    fun changeReaderBold(value: Boolean) {
        readerBold = value
        prefs.edit().putBoolean("reader_bold", value).apply()
        TuturiumCloud.markDirty(app)
    }

    fun changeFont(value: Float) {
        fontSize = value
        prefs.edit().putFloat("font_size", value).apply()
        TuturiumCloud.markDirty(app)
    }

    fun changeFollow(value: Boolean) {
        changeListeningOptions(listeningOptions.copy(autoScroll = value))
    }

    fun checkUpdate(automatic: Boolean = false) {
        if (checking || updateProgress != null ||
            (automatic && android.os.SystemClock.elapsedRealtime() < nextAutomaticUpdateCheck)) return
        checking = true
        if (!automatic) updateStatus = "Memeriksa GitHub…"
        viewModelScope.launch {
            try {
                val latest = Updates.latest()
                val changed = latest != release
                if (changed) verifiedApk = null
                release = latest
                nextAutomaticUpdateCheck = android.os.SystemClock.elapsedRealtime() + 60 * 60_000L
                updateStatus = if (latest == null) "Versi terbaru sudah terpasang." else "Pembaruan tersedia."
                if (latest != null && (!automatic || changed)) showUpdate = true
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                nextAutomaticUpdateCheck = android.os.SystemClock.elapsedRealtime() + 60_000L
                if (!automatic) updateStatus = (error as? UpdateCheckException)?.message
                    ?: "Pemeriksaan gagal. Periksa koneksi dan coba lagi."
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
        private val databaseWrites = ReadingService.positionDispatcher
    }
}
