package id.tuturium.app

import android.content.ActivityNotFoundException
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import id.tuturium.app.icons.*
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

@Composable
fun Tuturium(model: TuturiumModel) {
    val context = LocalContext.current
    val playback by ReadingService.state.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf("library") }
    val snackbar = remember { SnackbarHostState() }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) model.import(uri)
    }
    val openFile = {
        try {
            picker.launch((DocumentImporter.mimeFormats.keys + "application/octet-stream").toTypedArray())
        } catch (_: ActivityNotFoundException) {
            model.status = "Pemilih dokumen Android belum tersedia."
        }
    }
    LaunchedEffect(Unit) { model.checkUpdate(automatic = true) }
    LaunchedEffect(tab) { if (tab == "settings") model.loadVoices() }
    LaunchedEffect(model.status) {
        model.status?.let {
            snackbar.showSnackbar(it)
            model.status = null
        }
    }
    LaunchedEffect(playback.error) { playback.error?.let { snackbar.showSnackbar(it) } }
    BackHandler(model.showUpdate || model.reader != null || tab == "settings") {
        when {
            model.showUpdate -> model.showUpdate = false
            model.reader != null -> model.closeReader()
            else -> tab = "library"
        }
    }

    val dark = model.theme == "dark" || (model.theme == "system" && isSystemInDarkTheme())
    SideEffect {
        (context as? Activity)?.window?.let { window ->
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    TuturiumTheme(model.theme) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                if (model.reader == null && !model.showUpdate) {
                    Column {
                        AnimatedVisibility(playback.bookId != null && playback.total > 0) {
                            MiniPlayer(playback, onOpen = { playback.bookId?.let(model::open) }, onPlay = {
                                val action = if (playback.playing || playback.loading) ReadingService.PAUSE else ReadingService.PLAY
                                ReadingService.command(context, action)
                            })
                        }
                        NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                            NavigationBarItem(tab == "library", { tab = "library"; model.refresh() },
                                { Icon(Icons.AutoMirrored.Rounded.LibraryBooks, null) }, label = { Text("Pustaka") })
                            NavigationBarItem(tab == "settings", { tab = "settings" },
                                { Icon(Icons.Rounded.Tune, null) }, label = { Text("Setelan") })
                        }
                    }
                }
            }
        ) { padding ->
            val screen = when {
                model.showUpdate && model.release != null -> "update"
                model.reader != null -> "reader"
                else -> tab
            }
            Crossfade(screen, modifier = Modifier.fillMaxSize().padding(padding),
                animationSpec = tween(160), label = "Layar Tuturium") { target ->
                when (target) {
                    "update" -> model.release?.let { UpdateScreen(model, it) }
                    "reader" -> {
                        val book = model.reader
                        val document = model.content
                        if (book != null && document != null) Reader(model, book, document, playback) { tab = "settings" }
                    }
                    "settings" -> SettingsScreen(model)
                    else -> LibraryScreen(model, playback, openFile)
                }
            }
        }
        if (model.showPaste) PasteDialog(model)
        model.importing?.let { progress ->
            AlertDialog(onDismissRequest = {}, title = { Text("Mengimpor dokumen") },
                text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(progress)
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                } }, confirmButton = { TextButton(onClick = model::cancelImport) { Text("Batalkan") } })
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
        AlertDialog(onDismissRequest = { rename = null }, title = { Text("Ganti judul") },
            text = { OutlinedTextField(title, { title = it.take(200) }, label = { Text("Judul") }, singleLine = true) },
            confirmButton = { TextButton({ model.rename(book, title); rename = null }, enabled = title.isNotBlank()) { Text("Simpan") } },
            dismissButton = { TextButton({ rename = null }) { Text("Batal") } })
    }
    remove?.let { book ->
        AlertDialog(onDismissRequest = { remove = null }, title = { Text("Hapus dari pustaka?") },
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
                DropdownMenu(menu, { menu = false }) {
                    DropdownMenuItem(text = { Text("Ganti judul") }, onClick = { menu = false; rename() })
                    DropdownMenuItem(text = { Text("Hapus") }, onClick = { menu = false; remove() })
                }
            }
        }
    }
}

@Composable
private fun MiniPlayer(playback: Playback, onOpen: () -> Unit, onPlay: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Row(Modifier.padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f).heightIn(min = 52.dp).clickable(onClick = onOpen),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.GraphicEq, null, tint = MaterialTheme.colorScheme.primary)
                Text(playback.title, Modifier.padding(horizontal = 8.dp), style = MaterialTheme.typography.titleMedium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onPlay) {
                Icon(if (playback.playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    if (playback.playing) "Jeda" else "Putar")
            }
        }
    }
}

@Composable
private fun PasteDialog(model: TuturiumModel) {
    var title by rememberSaveable { mutableStateOf("") }
    AlertDialog(onDismissRequest = { model.showPaste = false }, title = { Text("Tambah teks") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(title, { title = it.take(200) }, label = { Text("Judul (opsional)") }, singleLine = true)
            OutlinedTextField(model.textDraft, { model.textDraft = it.take(DocumentImporter.MAX_TEXT) },
                label = { Text("Teks bacaan") }, minLines = 4, maxLines = 8)
        }
    }, confirmButton = {
        TextButton(onClick = { model.addText(title, model.textDraft); model.showPaste = false },
            enabled = model.textDraft.isNotBlank()) { Text("Simpan") }
    }, dismissButton = { TextButton({ model.showPaste = false }) { Text("Batal") } })
}
