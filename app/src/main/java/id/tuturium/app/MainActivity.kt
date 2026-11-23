package id.tuturium.app

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) handle(intent)
        setContent { Tuturium(model) }
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
        voiceProbe?.shutdown()
        voiceProbe = null
        voiceStatus = "Memuat suara Google Indonesia…"
        // Pass the engine package explicitly: never enumerate default-engine voices.
        try {
            voiceProbe = TextToSpeech(app, { result ->
                viewModelScope.launch(Dispatchers.Main) {
                    if (token != voiceLoadToken) return@launch
                    val googleReady = result == TextToSpeech.SUCCESS &&
                        runCatching { voiceProbe?.engines.orEmpty().any {
                            it.name == GOOGLE_TTS_ENGINE
                        } }.getOrDefault(false)
                    voices = if (googleReady) {
                        googleIndonesianVoices(
                            voiceProbe?.offlineVoices().orEmpty().map { it.choice() })
                    } else emptyList()
                    voiceStatus = when {
                        !googleReady -> "Google Speech Services belum tersedia."
                        voices.isEmpty() ->
                            "Suara Indonesia belum diunduh. Pasang data suara Google untuk menggunakannya."
                        else -> ""
                    }
                }
            }, GOOGLE_TTS_ENGINE)
        } catch (error: Exception) {
            android.util.Log.e("TuturiumVoice", "Google TTS cannot initialize", error)
            voices = emptyList()
            voiceStatus = "Google Speech Services tidak dapat dijalankan."
        }
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
        voiceProbe?.shutdown()
        super.onCleared()
    }

    companion object {
        private val databaseWrites = Dispatchers.IO.limitedParallelism(1)
    }
}
