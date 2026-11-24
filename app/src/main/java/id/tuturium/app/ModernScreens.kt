package id.tuturium.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val Tile = Color(0xFF202020)
private val Muted = Color(0xFF9D9D9D)
private val Accent = Color(0xFF5362F5)

@Composable
fun ModernHome(model: TuturiumModel, openProfile: () -> Unit, openBook: (Book) -> Unit,
    onFile: () -> Unit, onScan: () -> Unit, onLink: () -> Unit, onPaste: () -> Unit,
    onDictation: () -> Unit, onAdd: () -> Unit) {
    val recent = model.books.take(12)
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 3.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Surface(onClick = openProfile, modifier = Modifier.size(44.dp),
                    shape = CircleShape, color = Tile) {
                    HomeGoogleAvatar()
                }
                Spacer(Modifier.weight(1f))
                Image(painterResource(R.drawable.brand), "Logo Tuturium", Modifier.size(32.dp))
                Spacer(Modifier.width(7.dp))
                Text("Tuturium", style = MaterialTheme.typography.headlineSmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(44.dp))
            }
        }
        item {
            Text("Lanjutkan", style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp), color = Color.White)
            Spacer(Modifier.height(17.dp))
            if (recent.isEmpty()) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(22.dp)).background(Tile).padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Belum ada bacaan", style = MaterialTheme.typography.titleMedium)
                    Text("Tambahkan dokumen untuk mulai membaca dan mendengarkan.", color = Muted)
                    FilledTonalButton(onClick = onFile) { Icon(Icons.Rounded.Add, null); Text("Impor file") }
                }
            } else {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    items(recent, key = { it.id }) { book ->
                        Column(Modifier.width(96.dp).clickable { openBook(book) }) {
                            DocumentArt(book.format, Modifier.size(width = 96.dp, height = 116.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(book.title, maxLines = 2, overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall, color = Color.White, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }
        item {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text("Dengarkan", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Spacer(Modifier.height(17.dp))
                val actions = listOf(
                    Triple("File", Icons.Rounded.FolderOpen, onFile),
                    Triple("Pindai", Icons.Rounded.CameraAlt, onScan),
                    Triple("Tautan", Icons.Rounded.Link, onLink),
                    Triple("Tempel", Icons.Rounded.TextFields, onPaste),
                    Triple("Dikte", Icons.Rounded.Mic, onDictation),
                    Triple("Tambah", Icons.Rounded.Add, onAdd)
                )
                actions.chunked(3).forEachIndexed { index, row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        row.forEach { (title, icon, action) ->
                            Surface(onClick = action, shape = RoundedCornerShape(20.dp),
                                color = Tile, modifier = Modifier.weight(1f).height(96.dp)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center) {
                                    Icon(icon, null, Modifier.size(28.dp), tint = Color.White)
                                    Spacer(Modifier.height(8.dp))
                                    Text(title, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                    if (index == 0) Spacer(Modifier.height(9.dp))
                }
            }
        }
    }
}

@Composable
private fun DocumentArt(format: String, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxWidth(.77f).fillMaxHeight(.91f)
            .clip(RoundedCornerShape(topStart = 7.dp, topEnd = 17.dp, bottomStart = 7.dp, bottomEnd = 7.dp))
            .background(Color(0xFFF9F9FA))) {
            Column(Modifier.align(Alignment.TopStart).padding(start = 11.dp, top = 21.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(4) { idx ->
                    Box(Modifier.width(if (idx == 3) 25.dp else 39.dp).height(2.dp)
                        .background(Color(0xFFD7D7DB)))
                }
            }
            Text(format.take(5).uppercase(Locale.ROOT), modifier = Modifier.align(Alignment.BottomStart)
                .padding(start = 5.dp, bottom = 6.dp)
                .clip(RoundedCornerShape(3.dp)).background(Accent).padding(horizontal = 5.dp, vertical = 2.dp),
                color = Color.White, fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif)
        }
    }
}

@Composable
fun ModernLibrary(model: TuturiumModel, playback: Playback, onFolder: () -> Unit) {
    val context = LocalContext.current
    var folders by remember { mutableStateOf(FolderStore.names(context)) }
    var createFolder by remember { mutableStateOf(false) }
    var folderDraft by remember { mutableStateOf("") }
    var selectedFolder by remember { mutableStateOf<String?>(null) }
    var assignmentVersion by remember { mutableIntStateOf(0) }
    var search by rememberSaveable { mutableStateOf("") }
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var filter by rememberSaveable { mutableStateOf("Buku") }
    var sortTitle by rememberSaveable { mutableStateOf(false) }
    var rename by remember { mutableStateOf<Book?>(null) }
    var remove by remember { mutableStateOf<Book?>(null) }
    var menu by remember { mutableStateOf(false) }
    val visible = remember(model.books, search, filter, selectedFolder, assignmentVersion, sortTitle) {
        model.books.filter {
            it.title.contains(search, ignoreCase = true) &&
                (filter == "Buku" || (filter == "Folder" && selectedFolder != null &&
                    FolderStore.folderOf(context, it.id) == selectedFolder))
        }.let { if (sortTitle) it.sortedBy { book -> book.title.lowercase(Locale.ROOT) } else it }
    }

    LazyColumn(Modifier.fillMaxSize().background(Color.Black),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Perpustakaan", modifier = Modifier.weight(1f),
                    color = Color.White, style = MaterialTheme.typography.headlineLarge)
                IconButton(onClick = { createFolder = true }) { Icon(Icons.Rounded.CreateNewFolder, "Folder baru") }
                IconButton(onClick = { searchOpen = !searchOpen }) { Icon(Icons.Rounded.Search, "Cari dokumen") }
                Box {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Rounded.MoreHoriz, "Opsi perpustakaan") }
                    TuturiumMenu(menu, { menu = false }) {
                        DropdownMenuItem(text = { Text(if (sortTitle) "Urutkan terbaru" else "Urutkan A–Z") },
                            onClick = { sortTitle = !sortTitle; menu = false })
                    }
                }
            }
        }
        if (searchOpen) item {
            OutlinedTextField(value = search, onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                placeholder = { Text("Cari judul") }, leadingIcon = { Icon(Icons.Rounded.Search, null) })
        }
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Buku", "Podcast", "Folder").forEach { name ->
                    FilterChip(selected = filter == name, onClick = {
                        filter = name
                        if (name != "Folder") selectedFolder = null
                    },
                        label = { Text(name) }, shape = RoundedCornerShape(30.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Tile, selectedContainerColor = Color(0xFF303052),
                            labelColor = Color.White, selectedLabelColor = Color.White),
                        border = null)
                }
            }
        }
        if (filter == "Buku" && visible.isEmpty()) item {
            Text(if (search.isBlank()) "Belum ada dokumen. Tekan + untuk menambahkan."
                 else "Judul tidak ditemukan.", color = Muted,
                modifier = Modifier.padding(top = 22.dp))
        }
        if (filter == "Podcast") item {
            Text("Podcast belum tersedia di Tuturium.", color = Muted,
                modifier = Modifier.padding(top = 22.dp))
        }
        if (filter == "Folder" && selectedFolder == null) item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (folders.isEmpty()) Text("Belum ada folder. Buat folder untuk menyusun bacaan.", color = Muted)
                folders.forEach { folder ->
                    Surface(onClick = { selectedFolder = folder }, color = Tile,
                        shape = RoundedCornerShape(18.dp)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Folder, null)
                            Spacer(Modifier.width(12.dp))
                            Text(folder, Modifier.weight(1f))
                            Text("${model.books.count { FolderStore.folderOf(context, it.id) == folder }}",
                                color = Muted)
                            Icon(Icons.Rounded.ChevronRight, null, tint = Muted)
                        }
                    }
                }
                OutlinedButton(onClick = { createFolder = true }) {
                    Icon(Icons.Rounded.CreateNewFolder, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Folder baru")
                }
            }
        }
        if (filter == "Folder" && selectedFolder != null) item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { selectedFolder = null }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Daftar folder")
                }
                Text(selectedFolder.orEmpty(), style = MaterialTheme.typography.titleMedium)
            }
            if (visible.isEmpty()) Text("Belum ada dokumen di folder ini.", color = Muted)
        }
        if (filter == "Buku" || (filter == "Folder" && selectedFolder != null))
            items(visible, key = { it.id }) { book ->
            Row(Modifier.fillMaxWidth().clickable { model.open(book.id) }.heightIn(min = 78.dp),
                verticalAlignment = Alignment.CenterVertically) {
                DocumentArt(book.format, Modifier.size(width = 73.dp, height = 86.dp))
                Column(Modifier.weight(1f).padding(start = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(book.title, maxLines = 2, overflow = TextOverflow.Ellipsis,
                        color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    val current = if (playback.bookId == book.id && playback.total > 0) {
                        if (playback.completed) book.count else playback.index
                    } else book.position
                    val percentage = (current.toFloat() / book.count.coerceAtLeast(1) * 100).toInt().coerceIn(0, 100)
                    Text("$percentage%  ·  ${book.format}", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                var options by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { options = true }) { Icon(Icons.Rounded.MoreVert, "Opsi ${book.title}", tint = Muted) }
                    TuturiumMenu(options, onDismissRequest = { options = false }) {
                        DropdownMenuItem(text = { Text("Ganti judul") }, onClick = { options = false; rename = book })
                        folders.forEach { folder ->
                            DropdownMenuItem(text = { Text("Pindah ke: $folder") }, onClick = {
                                FolderStore.assign(context, book.id, folder)
                                assignmentVersion++
                                options = false
                            })
                        }
                        if (FolderStore.folderOf(context, book.id) != null) {
                            DropdownMenuItem(text = { Text("Keluarkan dari folder") }, onClick = {
                                FolderStore.assign(context, book.id, null)
                                assignmentVersion++
                                options = false
                            })
                        }
                        DropdownMenuItem(text = { Text("Pindah ke sampah") }, onClick = { options = false; remove = book })
                    }
                }
            }
        }
    }
    if (createFolder) TuturiumDialog(
        onDismissRequest = { createFolder = false },
        title = { Text("Folder baru") },
        text = { OutlinedTextField(folderDraft, { folderDraft = it.take(60) },
            singleLine = true, label = { Text("Nama folder") }) },
        confirmButton = { TextButton(onClick = {
            if (FolderStore.add(context, folderDraft)) {
                folders = FolderStore.names(context)
                filter = "Folder"
                selectedFolder = null
                folderDraft = ""
                createFolder = false
            }
        }, enabled = folderDraft.isNotBlank() &&
            folders.none { it.equals(folderDraft.trim(), true) }) { Text("Buat") } },
        dismissButton = { TextButton(onClick = { createFolder = false }) { Text("Batal") } }
    )
    rename?.let { book ->
        var title by remember(book.id) { mutableStateOf(book.title) }
        TuturiumDialog(onDismissRequest = { rename = null }, title = { Text("Ganti judul") },
            text = { OutlinedTextField(title, { title = it.take(200) }, singleLine = true) },
            confirmButton = { TextButton(onClick = { model.rename(book, title); rename = null },
                enabled = title.isNotBlank()) { Text("Simpan") } },
            dismissButton = { TextButton(onClick = { rename = null }) { Text("Batal") } })
    }
    remove?.let { book ->
        TuturiumDialog(onDismissRequest = { remove = null }, title = { Text("Pindahkan ke sampah?") },
            text = { Text("Dokumen dapat dipulihkan melalui Profil → File yang Dihapus.") },
            confirmButton = { TextButton(onClick = { model.remove(book); remove = null }) { Text("Pindahkan") } },
            dismissButton = { TextButton(onClick = { remove = null }) { Text("Batal") } })
    }
}

@Composable
fun ModernNavigation(tab: String, onHome: () -> Unit, onLibrary: () -> Unit, onAdd: () -> Unit) {
    // Scaffold does not apply window insets automatically to a custom bottomBar.
    // Keep the entire navigation, including its labels, above Android's 3-button bar.
    Surface(modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
        color = Color.Black, shadowElevation = 0.dp) {
        Row(Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceAround) {
            NavTab("Beranda", Icons.Rounded.Home, tab == "home", Modifier.weight(1f), onHome)
            NavTab("Perpustakaan", Icons.Rounded.Description, tab == "library", Modifier.weight(1f), onLibrary)
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Surface(onClick = onAdd, shape = CircleShape, color = Tile, modifier = Modifier.size(56.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Add, "Tambahkan bacaan", Modifier.size(32.dp), tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun NavTab(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean,
    modifier: Modifier = Modifier, action: () -> Unit) {
    Column(modifier.clickable(onClick = action).padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.width(62.dp).height(38.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (selected) Color(0xFF2D2D4D) else Color.Transparent),
            contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Text(title, style = MaterialTheme.typography.labelMedium, color = Color.White, maxLines = 1)
    }
}

@Composable
fun ModernProfile(onBack: () -> Unit, onAccount: () -> Unit, onStats: () -> Unit,
    onSettings: () -> Unit, onVoices: () -> Unit, onStorage: () -> Unit,
    onTrash: () -> Unit, onUpdate: () -> Unit, onAbout: () -> Unit) {
    val cloud by TuturiumCloud.state.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxSize().background(Color.Black),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { IconButton(onClick = onBack, modifier = Modifier.padding(top = 18.dp)) {
            Icon(Icons.Rounded.Close, "Tutup profil", Modifier.size(30.dp))
        } }
        item {
            Column(Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                GoogleAvatar(cloud.photoUrl, 102.dp,
                    if (cloud.email != null) "Foto profil Google" else "Profil tamu")
                Spacer(Modifier.height(10.dp))
                Text(if (cloud.email != null)
                    cloud.displayName?.takeIf { it.isNotBlank() }
                        ?: cloud.email?.substringBefore('@') ?: "Akun Google"
                    else "Akun tamu",
                    style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(if (cloud.email != null) "Masuk dengan Google"
                    else "Tuturium lokal · tanpa login",
                    color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            Surface(shape = RoundedCornerShape(22.dp), color = Tile, modifier = Modifier.fillMaxWidth()) {
                Column {
                    ProfileEntry(Icons.Rounded.AccountCircle, "Akun", onAccount,
                        cloud.email ?: "Login Google opsional")
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    ProfileEntry(Icons.Rounded.BarChart, "Statistik", onStats)
                }
            }
        }
        item { Text("Pengaturan", modifier = Modifier.padding(top = 11.dp), style = MaterialTheme.typography.titleMedium) }
        item {
            Surface(shape = RoundedCornerShape(22.dp), color = Tile, modifier = Modifier.fillMaxWidth()) {
                Column {
                    ProfileEntry(Icons.Rounded.Headphones, "Mendengarkan", onSettings)
                    ProfileEntry(Icons.Rounded.RecordVoiceOver, "Pustaka Suara", onVoices, "Suara Indonesia")
                    ProfileEntry(Icons.Rounded.Storage, "Penyimpanan & Data", onStorage)
                    ProfileEntry(Icons.Rounded.DeleteOutline, "File yang Dihapus", onTrash)
                }
            }
        }
        item { Text("Aplikasi", modifier = Modifier.padding(top = 11.dp), style = MaterialTheme.typography.titleMedium) }
        item {
            Surface(shape = RoundedCornerShape(22.dp), color = Tile, modifier = Modifier.fillMaxWidth()) {
                Column {
                    ProfileEntry(Icons.Rounded.SystemUpdate, "Pembaruan aplikasi", onUpdate, "Tuturium ${BuildConfig.VERSION_NAME}")
                    ProfileEntry(Icons.Rounded.Info, "Tentang Tuturium", onAbout, "Pembaca dokumen offline")
                }
            }
        }
    }
}

@Composable
private fun ProfileEntry(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String,
    onClick: () -> Unit, subtitle: String? = null) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).heightIn(min = 65.dp)
        .padding(horizontal = 19.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(25.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Muted)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = Muted)
    }
}

@Composable
fun ModernDetailHeader(title: String, back: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Kembali") }
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 6.dp))
    }
}

@Composable
fun ModernStorage(model: TuturiumModel, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Color.Black)) {
        ModernDetailHeader("Penyimpanan & Data", onBack)
        Surface(color = Tile, shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val cloud by TuturiumCloud.state.collectAsStateWithLifecycle()
                Text(if (cloud.connected) "Google Drive: ${cloud.email}. ${cloud.message}"
                    else "Google Drive belum terhubung.")
                Text("Dokumen: ${model.books.size}", style = MaterialTheme.typography.titleMedium)
                Text("File yang dihapus: ${model.deletedBooks.size}",
                    style = MaterialTheme.typography.titleMedium)
                Text("Versi aplikasi: ${BuildConfig.VERSION_NAME}", color = Muted)
            }
        }
    }
}

@Composable
fun ModernTrash(model: TuturiumModel, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Color.Black)) {
        ModernDetailHeader("File yang Dihapus", onBack)
        if (model.deletedBooks.isEmpty()) {
            Text("Tidak ada file di sampah.", modifier = Modifier.padding(22.dp), color = Muted)
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(model.deletedBooks, key = { it.id }) { book ->
                    Surface(color = Tile, shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp)) {
                            Text(book.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                TextButton(onClick = { model.restore(book) }) { Text("Pulihkan") }
                                var confirm by remember { mutableStateOf(false) }
                                TextButton(onClick = { confirm = true }) { Text("Hapus permanen") }
                                if (confirm) TuturiumDialog(onDismissRequest = { confirm = false },
                                    title = { Text("Hapus permanen?") }, text = { Text("Tindakan ini tidak dapat dibatalkan.") },
                                    confirmButton = { TextButton(onClick = {
                                        model.deletePermanently(book); confirm = false
                                    }) { Text("Hapus") } },
                                    dismissButton = { TextButton(onClick = { confirm = false }) { Text("Batal") } })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernStatistics(model: TuturiumModel, onBack: () -> Unit) {
    val context = LocalContext.current
    var period by rememberSaveable { mutableStateOf("Tahun") }
    var tick by remember { mutableIntStateOf(0) }
    // Reload when this screen is reopened and when a different time range is selected.
    val history = remember(period, tick) { ListeningStats.read(context) }
    val metrics = remember(history, period) { ListeningStats.summary(history, period) }
    val bookCount = model.books.size
    val totalMinutes = metrics.durationMs / 60_000
    val savedMinutes = metrics.savedMs / 60_000
    LazyColumn(Modifier.fillMaxSize().background(Color.Black),
        contentPadding = PaddingValues(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(23.dp)) {
        item { ModernDetailHeader("Statistik", onBack) }
        item {
            Text("Waktu Mendengarkan", modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.titleMedium)
            PeriodChooser(period, { period = it })
            MetricPair(when (period) {
                "Minggu" -> "${totalMinutes / 7}min"
                "Bulan" -> "${totalMinutes / 30}min"
                else -> "${totalMinutes / 365}min"
            }, "Rata-rata Harian", durationLabel(metrics.durationMs), "Total")
            MetricBars(metrics, countWords = false)
        }
        item {
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Color(0xFF383838))
            Spacer(Modifier.height(15.dp))
            Text("Kata-kata yang Didengarkan", modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.titleMedium)
            PeriodChooser(period, { period = it })
            MetricPair("${metrics.words / when (period) {
                "Minggu" -> 7
                "Bulan" -> 30
                else -> 365
            }}", "Rata-rata Harian", "%,d".format(Locale.forLanguageTag("id-ID"), metrics.words), "Total")
            MetricBars(metrics, countWords = true)
        }
        item {
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Color(0xFF383838))
            Spacer(Modifier.height(18.dp))
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    SummaryCell(Icons.Rounded.Headphones, durationLabel(metrics.durationMs), "Total diputar", Modifier.weight(1f))
                    SummaryCell(Icons.Rounded.Description, "%,d".format(Locale.forLanguageTag("id-ID"), metrics.words),
                        "Jumlah kata", Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth()) {
                    SummaryCell(Icons.Rounded.Timer, durationLabel(metrics.savedMs), "Total tabungan", Modifier.weight(1f))
                    SummaryCell(Icons.Rounded.Speed, String.format(Locale.forLanguageTag("id-ID"),
                        "%.1fx", metrics.averageSpeed), "Kecepatan Rata-Rata", Modifier.weight(1f))
                }
                HorizontalDivider(color = Color(0xFF383838))
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.AutoStories, null, tint = Muted)
                    Text("$bookCount", style = MaterialTheme.typography.titleLarge)
                    Text("File & Buku Audio", color = Muted)
                }
                Text("Statistik dihitung dari pemutaran Tuturium setelah pembaruan ini.",
                    color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun PeriodChooser(value: String, onChange: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        listOf("Minggu", "Bulan", "6 bulan", "Tahun").forEach { option ->
            FilterChip(selected = value == option, onClick = { onChange(option) },
                modifier = Modifier.weight(1f), label = { Text(option, maxLines = 1) },
                shape = RoundedCornerShape(24.dp),
                colors = FilterChipDefaults.filterChipColors(containerColor = Color.Black,
                    selectedContainerColor = Color(0xFF303052), selectedLabelColor = Color.White, labelColor = Color.White))
        }
    }
}

@Composable
private fun MetricPair(first: String, firstLabel: String, second: String, secondLabel: String) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Column(Modifier.weight(1f)) {
            Text(first, style = MaterialTheme.typography.headlineSmall)
            Text(firstLabel, style = MaterialTheme.typography.bodySmall)
        }
        Column(Modifier.weight(1f)) {
            Text(second, style = MaterialTheme.typography.headlineSmall)
            Text(secondLabel, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun MetricBars(metrics: StatsSummary, countWords: Boolean) {
    val values = if (countWords) metrics.buckets.map { it.words.toFloat() }
        else metrics.buckets.map { it.durationMs.toFloat() }
    val max = (values.maxOrNull() ?: 0f).coerceAtLeast(1f)
    Row(Modifier.fillMaxWidth().height(190.dp).padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom) {
        values.forEachIndexed { index, value ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxWidth().height((130f * value / max).coerceAtLeast(4f).dp)
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    .background(if (value > 0) if (countWords) Color(0xFFFF9200) else Accent
                        else Color(0xFF353535)))
                Spacer(Modifier.height(5.dp))
                Text(metrics.labels.getOrElse(index) { "" }, color = Muted,
                    fontSize = 10.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun SummaryCell(icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String, label: String, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = Muted, modifier = Modifier.size(26.dp))
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.bodySmall, color = Muted)
    }
}

private fun durationLabel(durationMs: Long): String {
    val minutes = durationMs / 60_000
    return if (minutes < 60) "${minutes}min" else "${minutes / 60}h ${minutes % 60}min"
}

@Composable
private fun HomeGoogleAvatar() {
    val cloud by TuturiumCloud.state.collectAsStateWithLifecycle()
    GoogleAvatar(cloud.photoUrl, 44.dp, "Buka profil")
}

@Composable
internal fun TuturiumAbout(onBack: () -> Unit) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    LazyColumn(Modifier.fillMaxSize().background(colors.background),
        contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Kembali") }
                Text("Tentang Tuturium", style = MaterialTheme.typography.titleLarge)
            }
        }
        item {
            Column(Modifier.fillMaxWidth().padding(vertical = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(shape = RoundedCornerShape(22.dp), color = colors.primaryContainer) {
                    Icon(Icons.Rounded.AutoStories, null, Modifier.padding(18.dp).size(48.dp), tint = colors.onPrimaryContainer)
                }
                Spacer(Modifier.height(12.dp))
                Text("Tuturium", style = MaterialTheme.typography.headlineMedium)
                Text("Versi ${BuildConfig.VERSION_NAME}", color = colors.onSurfaceVariant)
            }
        }
        item { Text("Pembaca dokumen untuk belajar dan mendengarkan buku. Impor PDF, teks, dan dokumen lainnya, lalu baca dengan suara Indonesia.") }
        item { HorizontalDivider() }
        item { Text("Suara dan privasi", style = MaterialTheme.typography.titleMedium) }
        item { Text("Mendukung suara offline setelah model diunduh, dan Google TTS sesuai ketersediaan perangkat. Dokumen dapat dibaca secara lokal; sinkronisasi Google Drive bersifat opsional.") }
        item { Text("Teknologi", style = MaterialTheme.typography.titleMedium) }
        item { Text("Android · Jetpack Compose · Media3 · PDFBox · ML Kit · Supertonic / sherpa-onnx", color = colors.onSurfaceVariant) }
        item {
            OutlinedButton(onClick = {
                runCatching { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://github.com/WynnDev-rill/Tuturium"))) }
            }, modifier = Modifier.fillMaxWidth()) { Text("Repository dan kode sumber") }
        }
    }
}
