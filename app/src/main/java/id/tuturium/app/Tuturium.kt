package id.tuturium.app

import android.content.ActivityNotFoundException
import android.app.Activity
import android.speech.RecognizerIntent
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tuturium(model: TuturiumModel) {
    val context = LocalContext.current
    val playback by ReadingService.chromeState.collectAsStateWithLifecycle(Playback())
    var tab by rememberSaveable { mutableStateOf("home") }
    var showAddSheet by remember { mutableStateOf(false) }
    var libraryFolder by remember { mutableStateOf<String?>(null) }
    var addFolderTarget by remember { mutableStateOf<String?>(null) }
    var showCreateFolder by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var folderRefresh by remember { mutableIntStateOf(0) }
    fun showAdd(target: String?) { addFolderTarget = target; model.importTargetFolder = null; showAddSheet = true }
    var showLinkDialog by remember { mutableStateOf(false) }
    var linkDraft by remember { mutableStateOf("") }
    val snackbar = remember { SnackbarHostState() }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) model.import(uri) else model.importTargetFolder = null
    }
    val openFile: () -> Unit = {
        try {
            picker.launch((DocumentImporter.mimeFormats.keys + "application/octet-stream").toTypedArray())
        } catch (_: ActivityNotFoundException) {
            model.status = "Pemilih dokumen Android belum tersedia."
        }
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener { result ->
                    if (result.text.isBlank()) { model.importTargetFolder = null; model.status = "Tidak ada tulisan yang terdeteksi." }
                    else model.addText("Hasil pindai", result.text, format = "SCAN")
                }
                .addOnFailureListener { model.importTargetFolder = null; model.status = "Pemindaian teks gagal." }
                .addOnCompleteListener { recognizer.close() }
        } else model.importTargetFolder = null
    }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) model.importTargetFolder = null else {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            try {
                recognizer.process(InputImage.fromFilePath(context, uri))
                    .addOnSuccessListener { result ->
                        if (result.text.isBlank()) { model.importTargetFolder = null; model.status = "Tidak ada tulisan yang terdeteksi." }
                        else model.addText("Hasil foto", result.text, format = "SCAN")
                    }.addOnFailureListener { model.importTargetFolder = null; model.status = "Pemindaian foto gagal." }
                    .addOnCompleteListener { recognizer.close() }
            } catch (_: Exception) { recognizer.close(); model.importTargetFolder = null; model.status = "Foto tidak dapat dibuka." }
        }
    }
    val photo: () -> Unit = { photoPicker.launch("image/*") }
    val scan: () -> Unit = {
        try { camera.launch(null) }
        catch (_: Exception) { model.status = "Aplikasi kamera belum tersedia." }
    }
    val dictation = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) model.addText("Hasil dikte", spoken) else model.importTargetFolder = null
        } else model.importTargetFolder = null
    }
    val dictate: () -> Unit = {
        try {
            dictation.launch(android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Ucapkan teks yang ingin disimpan")
            })
        } catch (_: ActivityNotFoundException) {
            model.status = "Pengenalan suara belum tersedia di perangkat ini."
        }
    }
    val paste: () -> Unit = { model.textDraft = ""; model.showPaste = true }
    val link: () -> Unit = { showLinkDialog = true }

    LaunchedEffect(Unit) { model.refresh(); model.checkUpdate(automatic = true) }
    LaunchedEffect(model.status) {
        model.status?.let {
            snackbar.showSnackbar(it)
            model.status = null
        }
    }
    LaunchedEffect(playback.error) { playback.error?.let { snackbar.showSnackbar(it) } }
    BackHandler(model.showUpdate || model.reader != null || tab !in listOf("home", "library") ||
        showAddSheet || showLinkDialog) {
        when {
            showLinkDialog -> { showLinkDialog = false; model.importTargetFolder = null }
            showAddSheet -> showAddSheet = false
            model.showUpdate -> model.showUpdate = false
            model.reader != null -> model.closeReader()
            tab in listOf("account", "stats", "settings", "voices", "storage", "trash", "updates", "about") -> tab = "profile"
            else -> tab = "home"
        }
    }

    val dark = model.theme == "dark" || (model.theme == "system" && isSystemInDarkTheme())
    LaunchedEffect(context, dark) {
        (context as? Activity)?.window?.let { window ->
            // Avoid Android's grey contrast scrim blending into the black bottom bar.
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                window.isNavigationBarContrastEnforced = false
            }
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    TuturiumTheme(model.theme) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                if (model.reader == null && !model.showUpdate && tab in listOf("home", "library")) {
                    Column {
                        ModernNavigation(tab,
                            onHome = { tab = "home"; libraryFolder = null; model.importTargetFolder = null; model.refresh() },
                            onLibrary = { tab = "library"; model.refresh() },
                            onAdd = { showAdd(if (tab == "library") libraryFolder else null) })
                    }
                }
            }
        ) { padding ->
            val screen = when {
                model.showUpdate && model.release != null -> "update"
                model.reader != null -> "reader"
                else -> tab
            }
            Crossfade(screen, modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
                animationSpec = tween(170), label = "Layar Tuturium") { target ->
                when (target) {
                    "update" -> model.release?.let { UpdateScreen(model, it) }
                    "reader" -> {
                        val book = model.reader
                        val document = model.content
                        if (book != null && document != null) Reader(model, book, document) { tab = "settings" }
                    }
                    "home" -> ModernHome(model,
                        openProfile = { tab = "profile" },
                        openBook = { model.open(it.id) },
                        onFile = openFile, onScan = scan, onLink = link,
                        onPaste = paste, onDictation = dictate, onAdd = { showAdd(null) })
                    "library" -> LibraryV2(model, playback,
                        onAdd = { showAdd(it) }, folderRefresh = folderRefresh,
                        onFolderChanged = { libraryFolder = it })
                    "profile" -> ModernProfile(
                        onBack = { tab = "home" },
                        onAccount = { tab = "account" },
                        onStats = { tab = "stats" },
                        onSettings = { tab = "settings" },
                        onVoices = { tab = "voices" },
                        onStorage = { model.refresh(); tab = "storage" },
                        onTrash = { model.refresh(); tab = "trash" },
                        onUpdate = { tab = "updates" }, onAbout = { tab = "about" })
                    "account" -> TuturiumAccountScreen(
                        onBack = { tab = "profile" },
                        onSignIn = { (context as? MainActivity)?.startGoogleSignIn() },
                        onRetryDrive = { (context as? MainActivity)?.retryGoogleDrive() },
                        onSignOut = { (context as? MainActivity)?.signOutGoogle() })
                    "stats" -> ModernStatistics(model, { tab = "profile" })
                    "settings" -> ListeningSettingsV2(model) { tab = "profile" }
                    "voices" -> VoiceLibraryV2(model) { tab = "profile" }
                    "storage" -> StorageV2(model, { tab = "profile" })
                    "trash" -> ModernTrash(model, { tab = "profile" })
                    "updates" -> ModernUpdateMenu(model, { tab = "profile" })
                    "about" -> TuturiumAbout { tab = "profile" }
                }
            }
        }
        if (showAddSheet) {
            TuturiumSheet(onDismissRequest = { showAddSheet = false },
                containerColor = MaterialTheme.colorScheme.surface) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
                    fun start(action: () -> Unit) {
                        showAddSheet = false
                        model.importTargetFolder = addFolderTarget
                        action()
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(Triple("Kamera", Icons.Rounded.CameraAlt, scan),
                            Triple("Foto", Icons.Rounded.Image, photo),
                            Triple("File", Icons.Rounded.AttachFile, openFile)).forEach { (title, icon, action) ->
                            Surface(onClick = { start(action) }, modifier = Modifier.weight(1f).height(90.dp),
                                shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Icon(icon, null, Modifier.size(30.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text(title, style = MaterialTheme.typography.titleSmall)
                                }
                            }
                        }
                    }
                    ListItem(headlineContent = { Text("Buat Folder") }, leadingContent = { Icon(Icons.Rounded.CreateNewFolder, null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { showAddSheet = false; newFolderName = ""; showCreateFolder = true })
                    HorizontalDivider()
                    listOf(
                        Triple("Tempel Link", Icons.Rounded.Link, link),
                        Triple("Ketik atau tempel teks", Icons.Rounded.EditNote, paste),
                        Triple("Teks yang Didiktekan", Icons.Rounded.Mic, dictate)
                    ).forEach { (title, icon, action) ->
                        ListItem(headlineContent = { Text(title) },
                            leadingContent = { Icon(icon, null) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable { start(action) })
                    }
                    HorizontalDivider()
                    Text("Diimpor dari", Modifier.padding(top = 14.dp, bottom = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                    ListItem(headlineContent = { Text("Penyimpanan perangkat / cloud") },
                        supportingContent = { Text("Google Drive jika tersedia di pemilih file Android") },
                        leadingContent = { Icon(Icons.Rounded.Cloud, null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { start(openFile) })
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
        if (showCreateFolder) TuturiumDialog(onDismissRequest = { showCreateFolder = false },
            title = { Text("Buat Folder") },
            text = { OutlinedTextField(newFolderName, { newFolderName = it.take(60) }, singleLine = true, label = { Text("Nama folder") }) },
            confirmButton = { TextButton(onClick = {
                if (FolderStore.add(context, newFolderName, model.importTargetFolder)) { folderRefresh++; showCreateFolder = false; tab = "library" }
                else model.status = "Nama folder sudah digunakan atau tidak valid."
            }) { Text("Buat") } },
            dismissButton = { TextButton(onClick = { showCreateFolder = false }) { Text("Batal") } })
        if (showLinkDialog) {
            TuturiumDialog(onDismissRequest = { showLinkDialog = false; model.importTargetFolder = null },
                title = { Text("Dengarkan tautan") },
                text = { OutlinedTextField(linkDraft, { linkDraft = it.take(2048) },
                    singleLine = true, label = { Text("URL HTTPS halaman web") }) },
                confirmButton = {
                    TextButton(onClick = {
                        model.importLink(linkDraft)
                        showLinkDialog = false
                        linkDraft = ""
                    }, enabled = linkDraft.startsWith("https://")) { Text("Impor") }
                },
                dismissButton = { TextButton(onClick = { showLinkDialog = false; model.importTargetFolder = null }) { Text("Batal") } })
        }
        if (model.showPaste) PasteDialog(model)
        model.importing?.let { progress ->
            TuturiumDialog(onDismissRequest = {}, title = { Text("Mengimpor dokumen") },
                text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(progress)
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                } }, confirmButton = { TextButton(onClick = model::cancelImport) { Text("Batalkan") } })
        }
    }
}

@Composable
private fun ModernUpdateMenu(model: TuturiumModel, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ModernDetailHeader("Pembaruan aplikasi", onBack)
        val release = model.release
        Card(Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(
            containerColor = if (release == null) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(R.drawable.brand), null, Modifier.size(50.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Tuturium", style = MaterialTheme.typography.titleLarge)
                        Text("Versi ${BuildConfig.VERSION_NAME}")
                    }
                }
                if (release != null) {
                    Text("Pembaruan tersedia", style = MaterialTheme.typography.titleMedium)
                    Text("Tuturium ${release.version} siap diunduh.")
                }
                Text(model.updateStatus ?: "Periksa versi terbaru melalui GitHub Releases.")
                Button(onClick = { model.checkUpdate() },
                    enabled = !model.checking && model.updateProgress == null) {
                    Icon(Icons.Rounded.SystemUpdate, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (model.checking) "Memeriksa…" else "Periksa pembaruan")
                }
                if (release != null) {
                    FilledTonalButton(onClick = { model.showUpdate = true }) { Text("Lihat dan unduh pembaruan") }
                }
            }
        }
    }
}

@Composable
private fun LibraryScreen(model: TuturiumModel, playback: Playback, openFile: () -> Unit) {
    var search by rememberSaveable { mutableStateOf("") }
    var rename by remember { mutableStateOf<Book?>(null) }
    var remove by remember { mutableStateOf<Book?>(null) }
    var sortByTitle by rememberSaveable { mutableStateOf(false) }
    val books = model.books.filter { it.title.contains(search.trim(), ignoreCase = true) }
        .let { if (sortByTitle) it.sortedBy { book -> book.title.lowercase(Locale.ROOT) } else it }

    LazyColumn(contentPadding = PaddingValues(start = 22.dp, top = 14.dp, end = 22.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.brand), null, Modifier.size(35.dp))
                Spacer(Modifier.width(10.dp))
                Text("Tuturium", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = openFile) { Icon(Icons.Rounded.Add, "Impor dokumen") }
            }
        }
        item {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
                    .background(Brush.linearGradient(listOf(HeroStart, HeroEnd))).padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Text("Pustaka saya", style = MaterialTheme.typography.displaySmall, color = HeroInk)
                Text("Impor PDF, EPUB, DOCX atau TXT untuk dibaca dan didengarkan.",
                    color = Color(0xFFE4DBFF), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(3.dp))
                Button(openFile, colors = ButtonDefaults.buttonColors(
                    containerColor = HeroInk, contentColor = HeroStart)) {
                    Icon(Icons.Rounded.FileOpen, null, Modifier.size(19.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Impor dokumen")
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Dokumen", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                TextButton(onClick = { model.textDraft = ""; model.showPaste = true }) {
                    Icon(Icons.Rounded.EditNote, null)
                    Spacer(Modifier.width(5.dp))
                    Text("Tambah teks")
                }
            }
        }
        if (model.books.isNotEmpty()) item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(search, { search = it }, singleLine = true, modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Rounded.Search, null) }, label = { Text("Cari judul") },
                    shape = RoundedCornerShape(18.dp))
                IconButton(onClick = { sortByTitle = !sortByTitle }) {
                    Icon(Icons.Rounded.SortByAlpha, if (sortByTitle) "Urutkan berdasarkan aktivitas" else "Urutkan menurut judul")
                }
            }
        }
        if (books.isEmpty()) item {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.AutoStories, null, tint = MaterialTheme.colorScheme.primary)
                    Text(if (search.isBlank()) "Pustaka masih kosong" else "Judul tidak ditemukan",
                        style = MaterialTheme.typography.titleMedium)
                    if (search.isBlank()) {
                        Text("Buka berkas dari perangkat atau tempelkan teks untuk mulai.")
                        TextButton(onClick = {
                            model.addText("Sepucuk surat untuk pembaca",
                                "Di rak dekat jendela, buku-buku menunggu untuk dibuka. " +
                                    "Pagi itu, hujan jatuh perlahan di balik kaca.\n\n" +
                                    "Kamu bisa mengikuti kata yang disorot atau mematikan layar dan tetap mendengarkan. " +
                                    "Coba ubah kecepatan atau tandai kalimat yang ingin kamu temukan lagi.")
                        }) { Text("Buka contoh bacaan") }
                    }
                }
            }
        }
        items(books, key = { it.id }) { book ->
            BookRow(book, playback, { model.open(book.id) }, { rename = book }, { remove = book })
        }
    }
    rename?.let { book ->
        var title by remember(book.id) { mutableStateOf(book.title) }
        TuturiumDialog(onDismissRequest = { rename = null }, title = { Text("Ganti judul") },
            text = { OutlinedTextField(title, { title = it.take(200) }, label = { Text("Judul") }, singleLine = true) },
            confirmButton = { TextButton({ model.rename(book, title); rename = null }, enabled = title.isNotBlank()) { Text("Simpan") } },
            dismissButton = { TextButton({ rename = null }) { Text("Batal") } })
    }
    remove?.let { book ->
        TuturiumDialog(onDismissRequest = { remove = null }, title = { Text("Hapus dari pustaka?") },
            text = { Text("Dokumen “${book.title}” beserta progres dan markahnya akan dihapus dari Tuturium.") },
            confirmButton = { TextButton({ model.remove(book); remove = null }) { Text("Hapus") } },
            dismissButton = { TextButton({ remove = null }) { Text("Batal") } })
    }
}

@Composable
private fun BookRow(book: Book, playback: Playback, open: () -> Unit, rename: () -> Unit, remove: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    val position = when {
        playback.bookId != book.id -> book.position
        playback.completed -> book.count
        playback.total > 0 -> playback.index
        else -> book.position
    }
    val fraction = (position.toFloat() / book.count).coerceIn(0f, 1f)
    Surface(onClick = open, shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(55.dp).clip(RoundedCornerShape(15.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center) {
                Text(book.format, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(book.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(when {
                    fraction >= 1f -> "Selesai"
                    position == 0 -> "Belum dibaca"
                    else -> "${(fraction * 100).toInt()}% dibaca"
                }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (position > 0) LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth().height(3.dp))
            }
            Box {
                IconButton({ menu = true }) { Icon(Icons.Rounded.MoreVert, "Opsi ${book.title}") }
                TuturiumMenu(menu, { menu = false }) {
                    DropdownMenuItem(text = { Text("Ganti judul") }, onClick = { menu = false; rename() })
                    DropdownMenuItem(text = { Text("Hapus") }, onClick = { menu = false; remove() })
                }
            }
        }
    }
}

@Composable
private fun PasteDialog(model: TuturiumModel) {
    var title by rememberSaveable { mutableStateOf("") }
    TuturiumDialog(onDismissRequest = { model.showPaste = false; model.importTargetFolder = null }, title = { Text("Tambah teks") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(title, { title = it.take(200) }, label = { Text("Judul (opsional)") }, singleLine = true)
            OutlinedTextField(model.textDraft, { model.textDraft = it.take(DocumentImporter.MAX_TEXT) },
                label = { Text("Teks bacaan") }, minLines = 4, maxLines = 8)
        }
    }, confirmButton = {
        TextButton(onClick = { model.addText(title, model.textDraft); model.showPaste = false },
            enabled = model.textDraft.isNotBlank()) { Text("Simpan") }
    }, dismissButton = { TextButton({ model.showPaste = false; model.importTargetFolder = null }) { Text("Batal") } })
}
