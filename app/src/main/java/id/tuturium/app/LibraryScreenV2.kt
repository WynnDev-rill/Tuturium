package id.tuturium.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DateFormat
import java.util.Date
import java.util.Locale

private val Panel = Color(0xFF202020)
private val Blue = Color(0xFF5360FA)
private val Secondary = Color(0xFF9D9D9D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryV2(model: TuturiumModel, playback: Playback, onAdd: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("library_display", 0) }
    var folders by remember { mutableStateOf(FolderStore.names(context)) }
    var folder by rememberSaveable { mutableStateOf<String?>(null) }
    var filter by rememberSaveable { mutableStateOf("Buku") }
    var sort by rememberSaveable { mutableStateOf(prefs.getString("sort", "Terbaru") ?: "Terbaru") }
    var layout by rememberSaveable { mutableStateOf(prefs.getString("view", "Daftar") ?: "Daftar") }
    var groupByFormat by rememberSaveable { mutableStateOf(prefs.getBoolean("group", false)) }
    var search by rememberSaveable { mutableStateOf("") }
    var searching by rememberSaveable { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    var filterMenu by remember { mutableStateOf(false) }
    var selecting by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var activeFile by remember { mutableStateOf<Book?>(null) }
    var moveFile by remember { mutableStateOf<Book?>(null) }
    var renameFile by remember { mutableStateOf<Book?>(null) }
    var deleteFile by remember { mutableStateOf<Book?>(null) }
    var folderOptions by remember { mutableStateOf<String?>(null) }
    var renameFolder by remember { mutableStateOf<String?>(null) }
    var deleteFolder by remember { mutableStateOf<String?>(null) }
    var folderEditor by remember { mutableStateOf(false) }
    var folderDraft by remember { mutableStateOf("") }
    var choosingForFolder by remember { mutableStateOf(false) }
    var chooseTargetForSelection by remember { mutableStateOf(false) }
    var revision by remember { mutableIntStateOf(0) }
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = remember { FocusRequester() }
    val allFolders = remember(folders, revision) {
        folders.sortedWith(compareByDescending<String> { FolderStore.isPinned(context, it) }
            .thenBy { it.lowercase(Locale.ROOT) })
    }
    val visible = remember(model.books, search, filter, folder, sort, revision) {
        sortBooks(model.books.filter { book ->
            (folder == null || FolderStore.folderOf(context, book.id) == folder) &&
            matchesLibraryFilter(book, filter, BookActions.later(context, book.id)) &&
            book.title.contains(search.trim(), ignoreCase = true)
        }, sort).sortedWith(compareByDescending<Book> { BookActions.pinned(context, it.id) })
    }
    val shownFolders = if (search.isBlank()) allFolders else allFolders.filter {
        it.contains(search.trim(), ignoreCase = true)
    }
    BackHandler(searching || folder != null || selecting) {
        when {
            searching -> { searching = false; search = ""; keyboard?.hide() }
            selecting -> { selectedIds = emptySet(); selecting = false }
            else -> folder = null
        }
    }
    LazyColumn(Modifier.fillMaxSize().background(Color.Black),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(Modifier.fillMaxWidth().heightIn(min = 54.dp),
                verticalAlignment = Alignment.CenterVertically) {
                when {
                    searching -> {
                        IconButton(onClick = { searching = false; search = ""; keyboard?.hide() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Tutup pencarian")
                        }
                        BasicTextField(search, { search = it }, singleLine = true,
                            textStyle = MaterialTheme.typography.titleLarge.copy(color = Color.White),
                            cursorBrush = SolidColor(Blue),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                            modifier = Modifier.weight(1f).focusRequester(focus),
                            decorationBox = { input ->
                                Box {
                                    if (search.isBlank()) Text("Cari", color = Secondary,
                                        style = MaterialTheme.typography.titleLarge)
                                    input()
                                }
                            })
                        IconButton(onClick = { search = "" }) {
                            Icon(Icons.Rounded.Close, "Hapus pencarian", tint = Secondary)
                        }
                    }
                    selecting -> {
                        IconButton(onClick = { selecting = false; selectedIds = emptySet() }) {
                            Icon(Icons.Rounded.Close, "Batal pilih")
                        }
                        Text("${selectedIds.size} dipilih", Modifier.weight(1f),
                            style = MaterialTheme.typography.titleLarge)
                        IconButton(onClick = { chooseTargetForSelection = true }, enabled = selectedIds.isNotEmpty()) {
                            Icon(Icons.Rounded.DriveFileMove, "Pindahkan yang dipilih")
                        }
                        IconButton(onClick = {
                            model.removeMany(model.books.filter { it.id in selectedIds })
                            selectedIds = emptySet(); selecting = false
                        }, enabled = selectedIds.isNotEmpty()) {
                            Icon(Icons.Rounded.DeleteOutline, "Hapus yang dipilih", tint = Color.Red)
                        }
                    }
                    else -> {
                        if (folder != null) IconButton(onClick = { folder = null }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Daftar folder")
                        }
                        Text(folder ?: "Perpustakaan", Modifier.weight(1f),
                            style = MaterialTheme.typography.headlineLarge, maxLines = 1,
                            overflow = TextOverflow.Ellipsis)
                        if (folder == null) {
                            IconButton(onClick = { folderDraft = ""; folderEditor = true }) {
                                Icon(Icons.Rounded.CreateNewFolder, "Buat folder")
                            }
                            IconButton(onClick = { searching = true }) {
                                Icon(Icons.Rounded.Search, "Cari dokumen")
                            }
                        }
                        Box {
                            IconButton(onClick = { if (folder == null) menu = true else folderOptions = folder }) {
                                Icon(Icons.Rounded.MoreHoriz, "Opsi perpustakaan")
                            }
                            DropdownMenu(expanded = menu, onDismissRequest = { menu = false },
                                modifier = Modifier.width(275.dp).background(Panel)) {
                                DropdownMenuItem(text = { Text("Pilih beberapa") },
                                    leadingIcon = { Icon(Icons.Rounded.Checklist, null) },
                                    onClick = { selecting = true; menu = false })
                                HorizontalDivider(color = Color.DarkGray)
                                Text("Lihat", Modifier.padding(start = 16.dp, top = 12.dp, bottom = 6.dp),
                                    color = Secondary, style = MaterialTheme.typography.labelMedium)
                                listOf(
                                    Triple("Daftar", Icons.Rounded.ViewList, layout == "Daftar"),
                                    Triple("Kotak-kotak Besar", Icons.Rounded.GridView, layout == "Kotak-kotak Besar"),
                                    Triple("Kotak Kecil", Icons.Rounded.Apps, layout == "Kotak Kecil")
                                ).forEach { (name, icon, active) ->
                                    DropdownMenuItem(text = { Text(name) }, leadingIcon = { Icon(icon, null) },
                                        trailingIcon = { if (active) Icon(Icons.Rounded.Check, null) },
                                        onClick = {
                                            layout = name; prefs.edit().putString("view", name).apply(); menu = false
                                        })
                                }
                                HorizontalDivider(color = Color.DarkGray)
                                Text("Urutan", Modifier.padding(start = 16.dp, top = 12.dp, bottom = 6.dp),
                                    color = Secondary, style = MaterialTheme.typography.labelMedium)
                                listOf(
                                    Triple("Terbaru", Icons.Rounded.Schedule, sort == "Terbaru"),
                                    Triple("Tanggal ditambahkan", Icons.Rounded.CalendarMonth,
                                        sort == "Tanggal ditambahkan"),
                                    Triple("Abjad", Icons.Rounded.SortByAlpha, sort == "Abjad")
                                ).forEach { (name, icon, active) ->
                                    DropdownMenuItem(text = { Text(name) }, leadingIcon = { Icon(icon, null) },
                                        trailingIcon = { if (active) Icon(Icons.Rounded.Check, null) },
                                        onClick = {
                                            sort = name; prefs.edit().putString("sort", name).apply(); menu = false
                                        })
                                }
                                HorizontalDivider(color = Color.DarkGray)
                                DropdownMenuItem(text = { Text("Kelompokkan berdasarkan format") },
                                    leadingIcon = { Icon(Icons.Rounded.Category, null) },
                                    trailingIcon = { if (groupByFormat) Icon(Icons.Rounded.Check, null) },
                                    onClick = {
                                        groupByFormat = !groupByFormat
                                        prefs.edit().putBoolean("group", groupByFormat).apply()
                                        menu = false
                                    })
                            }
                        }
                    }
                }
            }
        }
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box {
                    FilledIconButton(onClick = { filterMenu = true },
                        modifier = Modifier.size(44.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Panel, contentColor = Color.White)) {
                        Icon(Icons.Rounded.FilterList, "Filter perpustakaan")
                    }
                    DropdownMenu(expanded = filterMenu, onDismissRequest = { filterMenu = false },
                        modifier = Modifier.width(240.dp),
                        shape = RoundedCornerShape(20.dp),
                        containerColor = Color(0xFF2D2D2D)) {
                                listOf(
                                    "Belum dibaca" to Icons.Rounded.AutoStories,
                                    "Baca nanti" to Icons.Rounded.Schedule,
                                    "PDF" to Icons.Rounded.PictureAsPdf,
                                    "Dokumen" to Icons.Rounded.Description,
                                    "Halaman web" to Icons.Rounded.Language,
                                    "Pindai" to Icons.Rounded.CameraAlt,
                                    "Lainnya" to Icons.Rounded.MoreHoriz
                                ).forEach { (name, icon) ->
                                    if (name == "PDF") HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(horizontal = 16.dp))
                                    DropdownMenuItem(text = { Text(name) },
                                        leadingIcon = { Icon(icon, null) },
                                        trailingIcon = { if (filter == name) Icon(Icons.Rounded.Check, null) },
                                        onClick = { filter = name; filterMenu = false })
                                }
                    }
                }
                if (filter !in listOf("Buku", "Folder")) {
                    FilterChip(selected = true, onClick = { filter = "Buku" },
                        label = { Text(filter) }, trailingIcon = { Icon(Icons.Rounded.Close, "Hapus filter") },
                        shape = RoundedCornerShape(26.dp), border = null)
                }
                listOf("Buku", "Folder").forEach { name ->
                    FilterChip(selected = filter == name, onClick = { filter = name },
                        label = { Text(name) }, shape = RoundedCornerShape(26.dp),
                        border = null, colors = FilterChipDefaults.filterChipColors(
                            containerColor = Panel, selectedContainerColor = Color(0xFF343450),
                            labelColor = Color.White, selectedLabelColor = Color.White))
                }
            }
        }
        run {
            if (folder == null && filter in listOf("Buku", "Folder")) {
                items(shownFolders, key = { "folder:$it" }) { name ->
                    Row(Modifier.fillMaxWidth().height(82.dp)
                        .clickable { folder = name; filter = "Buku" },
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Folder, null, Modifier.size(60.dp), tint = Color(0xFF2F9ED6))
                        Spacer(Modifier.width(12.dp))
                        Text(name, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { folderOptions = name }) {
                            Icon(Icons.Rounded.MoreHoriz, "Opsi folder $name", tint = Secondary)
                        }
                    }
                }
            }
            if (filter != "Folder" || folder != null) {
                if (groupByFormat && !selecting) {
                    visible.groupBy { it.format }.forEach { (format, group) ->
                        item(key = "group:$format") {
                            Text(format, color = Secondary, style = MaterialTheme.typography.labelLarge)
                        }
                        bookRows(group, layout, model, playback, selecting, selectedIds,
                            onClick = {
                                if (selecting) selectedIds = toggleSelected(selectedIds, it.id)
                                else model.open(it.id)
                            }, onOptions = { activeFile = it })
                    }
                } else {
                    bookRows(visible, layout, model, playback, selecting, selectedIds,
                        onClick = {
                            if (selecting) selectedIds = toggleSelected(selectedIds, it.id)
                            else model.open(it.id)
                        }, onOptions = { activeFile = it })
                }
            }
            if ((filter == "Folder" && shownFolders.isEmpty()) ||
                (filter != "Folder" && visible.isEmpty() &&
                    (folder != null || filter != "Buku" || shownFolders.isEmpty()))) item {
                Column(Modifier.fillMaxWidth().padding(top = 65.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(17.dp)) {
                    Icon(if (folder == null) Icons.Rounded.MenuBook else Icons.Rounded.FolderOpen,
                        null, Modifier.size(105.dp), tint = Color(0xFF444444))
                    Text(when {
                        search.isNotBlank() -> "Tidak ada hasil pencarian."
                        folder != null -> "Folder ini kosong. Impor atau pindahkan file ke sini."
                        filter == "Baca nanti" -> "Belum ada bacaan untuk nanti."
                        else -> "Belum ada dokumen."
                    }, color = Secondary)
                    Button(onClick = {
                        if (folder != null && model.books.isNotEmpty()) choosingForFolder = true
                        else onAdd()
                    }, colors = ButtonDefaults.buttonColors(containerColor = Blue)) {
                        Icon(Icons.Rounded.Add, null); Spacer(Modifier.width(6.dp))
                        Text("Tambahkan")
                    }
                }
            }
        }
    }
    LaunchedEffect(searching) { if (searching) { focus.requestFocus(); keyboard?.show() } }
    if (folderEditor) FolderEditorV2(folderDraft, { folderDraft = it },
        "Buat Folder", { folderEditor = false }, {
            if (FolderStore.add(context, it)) {
                folders = FolderStore.names(context); folder = it.trim().take(60)
                filter = "Buku"; folderEditor = false; revision++
            } else model.status = "Nama folder tidak valid atau sudah digunakan."
        })
    renameFolder?.let { name ->
        FolderEditorV2(folderDraft, { folderDraft = it },
            "Ubah Nama Folder", { renameFolder = null }, { new ->
                if (FolderStore.rename(context, name, new)) {
                    if (folder == name) folder = new.trim().take(60)
                    folders = FolderStore.names(context); renameFolder = null; revision++
                } else model.status = "Nama folder sudah digunakan."
            })
    }
    deleteFolder?.let { name ->
        AlertDialog(onDismissRequest = { deleteFolder = null },
            title = { Text("Hapus folder?") },
            text = { Text("Folder “$name” dihapus, tetapi semua dokumen tetap tersedia di Perpustakaan.") },
            confirmButton = { TextButton(onClick = {
                FolderStore.remove(context, name)
                if (folder == name) folder = null
                folders = FolderStore.names(context); deleteFolder = null; revision++
            }) { Text("Hapus") } },
            dismissButton = { TextButton(onClick = { deleteFolder = null }) { Text("Batal") } })
    }
    folderOptions?.let { name ->
        ModalBottomSheet(onDismissRequest = { folderOptions = null }, containerColor = Panel) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 10.dp)) {
                SheetHeadline(Icons.Rounded.Folder, name)
                Spacer(Modifier.height(14.dp))
                SheetAction(Icons.Rounded.PushPin,
                    if (FolderStore.isPinned(context, name)) "Lepas sematan" else "Sematkan di atas") {
                    FolderStore.togglePinned(context, name); folderOptions = null; revision++
                }
                SheetAction(Icons.Rounded.Edit, "Ubah nama") {
                    folderDraft = name; renameFolder = name; folderOptions = null
                }
                SheetAction(Icons.Rounded.DeleteOutline, "Hapus folder", danger = true) {
                    deleteFolder = name; folderOptions = null
                }
                Spacer(Modifier.height(14.dp))
            }
        }
    }
    activeFile?.let { book ->
        ModalBottomSheet(onDismissRequest = { activeFile = null }, containerColor = Panel) {
            Column(Modifier.fillMaxWidth().heightIn(max = 680.dp)
                .verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Description, null, Modifier.size(48.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(book.title, maxLines = 2, overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium)
                        Text("${DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.forLanguageTag("id-ID")).format(Date(book.created))} · ${book.words} kata · ${book.format}",
                            color = Secondary, style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { model.shareBook(book); activeFile = null }) {
                        Icon(Icons.Rounded.Share, "Bagikan")
                    }
                }
                Spacer(Modifier.height(14.dp))
                SheetAction(Icons.Rounded.PushPin,
                    if (BookActions.pinned(context, book.id)) "Lepas sematan" else "Sematkan di atas") {
                    BookActions.togglePin(context, book.id); revision++; activeFile = null
                }
                SheetAction(Icons.Rounded.Schedule,
                    if (BookActions.later(context, book.id)) "Hapus dari Baca nanti" else "Baca nanti") {
                    BookActions.toggleLater(context, book.id); revision++; activeFile = null
                }
                HorizontalDivider(color = Color.DarkGray)
                SheetAction(Icons.Rounded.PlaylistPlay, "Mainkan Berikutnya") {
                    BookActions.enqueue(context, book.id, true)
                    model.status = "Ditambahkan sebagai bacaan berikutnya."; activeFile = null
                }
                SheetAction(Icons.Rounded.PlaylistAdd, "Tambahkan ke Antrean") {
                    BookActions.enqueue(context, book.id)
                    model.status = "Ditambahkan ke antrean mendengarkan."; activeFile = null
                }
                HorizontalDivider(color = Color.DarkGray)
                SheetAction(Icons.Rounded.CheckCircleOutline, "Pilih") {
                    selecting = true; selectedIds = toggleSelected(selectedIds, book.id); activeFile = null
                }
                SheetAction(Icons.Rounded.DriveFileMove, "Pindahkan ke folder") {
                    moveFile = book; activeFile = null
                }
                SheetAction(Icons.Rounded.Edit, "Ubah nama") {
                    renameFile = book; activeFile = null
                }
                SheetAction(Icons.Rounded.CloudDone, "Tersimpan offline") {
                    model.status = "Tuturium menyimpan dokumen di perangkat, tanpa cloud."
                    activeFile = null
                }
                HorizontalDivider(color = Color.DarkGray)
                SheetAction(Icons.Rounded.DeleteOutline, "Hapus", danger = true) {
                    deleteFile = book; activeFile = null
                }
                Spacer(Modifier.height(14.dp))
            }
        }
    }
    if (moveFile != null || chooseTargetForSelection || choosingForFolder) {
        val moving = moveFile
        ModalBottomSheet(onDismissRequest = {
            moveFile = null; chooseTargetForSelection = false; choosingForFolder = false
        }, containerColor = Panel) {
            Column(Modifier.fillMaxWidth().heightIn(max = 680.dp)
                .verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 12.dp)) {
                Text(if (choosingForFolder) "Tambahkan bacaan" else "Pindahkan ke folder",
                    style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                if (choosingForFolder) {
                    model.books.filter { FolderStore.folderOf(context, it.id) != folder }.forEach { book ->
                        SheetAction(Icons.Rounded.Description, book.title) {
                            FolderStore.assign(context, book.id, folder)
                            choosingForFolder = false; revision++
                        }
                    }
                } else {
                    if (folders.isEmpty()) Text("Buat folder terlebih dahulu.", color = Secondary)
                    folders.forEach { name ->
                        SheetAction(Icons.Rounded.Folder, name) {
                            val ids = if (moving != null) listOf(moving.id) else selectedIds.toList()
                            ids.forEach { FolderStore.assign(context, it, name) }
                            selectedIds = emptySet(); selecting = false
                            moveFile = null; chooseTargetForSelection = false; revision++
                        }
                    }
                    SheetAction(Icons.Rounded.CreateNewFolder, "Buat folder baru") {
                        moveFile = null; chooseTargetForSelection = false
                        folderDraft = ""; folderEditor = true
                    }
                }
                Spacer(Modifier.height(14.dp))
            }
        }
    }
    renameFile?.let { book ->
        var value by remember(book.id) { mutableStateOf(book.title) }
        AlertDialog(onDismissRequest = { renameFile = null },
            title = { Text("Ubah nama") },
            text = { OutlinedTextField(value, { value = it.take(200) }, singleLine = true,
                modifier = Modifier.fillMaxWidth()) },
            confirmButton = { TextButton(onClick = { model.rename(book, value); renameFile = null },
                enabled = value.isNotBlank()) { Text("Simpan") } },
            dismissButton = { TextButton(onClick = { renameFile = null }) { Text("Batal") } })
    }
    deleteFile?.let { book ->
        AlertDialog(onDismissRequest = { deleteFile = null },
            title = { Text("Hapus dokumen?") },
            text = { Text("Bacaan dipindahkan ke File yang Dihapus dan dapat dipulihkan.") },
            confirmButton = { TextButton(onClick = { model.remove(book); deleteFile = null }) {
                Text("Hapus") } },
            dismissButton = { TextButton(onClick = { deleteFile = null }) { Text("Batal") } })
    }
}

private fun toggleSelected(ids: Set<String>, id: String) =
    if (id in ids) ids - id else ids + id

private fun LazyListScope.bookRows(
    books: List<Book>, layout: String, model: TuturiumModel, playback: Playback,
    selecting: Boolean, selected: Set<String>, onClick: (Book) -> Unit, onOptions: (Book) -> Unit
) {
    if (layout == "Daftar") items(books, key = { "book:${it.id}" }) { book ->
        Row(Modifier.fillMaxWidth().heightIn(min = 82.dp).clickable { onClick(book) },
            verticalAlignment = Alignment.CenterVertically) {
            FileArtwork(book.format, Modifier.size(69.dp, 80.dp))
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Text(book.title, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(7.dp))
                val current = if (playback.bookId == book.id && playback.total > 0)
                    if (playback.completed) book.count else playback.index else book.position
                val percent = (current.toFloat() / book.count.coerceAtLeast(1) * 100).toInt().coerceIn(0, 100)
                val minutes = estimateRemainingMinutes(book.words, book.count, current, model.settings.speed)
                Text("$percent% · ±$minutes menit · ${book.format}", color = Secondary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (model.savingBookId == book.id) {
                CircularProgressIndicator(Modifier.padding(12.dp).size(20.dp), strokeWidth = 2.dp,
                    color = Blue)
            } else if (selecting) Checkbox(checked = book.id in selected,
                onCheckedChange = { onClick(book) })
            else IconButton(onClick = { onOptions(book) }) {
                Icon(Icons.Rounded.MoreHoriz, "Opsi ${book.title}", tint = Secondary)
            }
        }
    } else {
        val cols = if (layout == "Kotak Kecil") 3 else 2
        items(books.chunked(cols), key = { row -> row.joinToString("-") { it.id } }) { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { book ->
                    Column(Modifier.weight(1f).clickable { onClick(book) }
                        .padding(bottom = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        FileArtwork(book.format,
                            Modifier.size(if (cols == 2) 105.dp else 82.dp,
                                if (cols == 2) 122.dp else 98.dp))
                        Text(book.title, maxLines = 2, overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall)
                        Text("±${estimateRemainingMinutes(book.words, book.count, book.position, model.settings.speed)} menit",
                            color = Secondary, style = MaterialTheme.typography.labelSmall)
                        if (model.savingBookId == book.id) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Blue)
                        } else if (selecting) Checkbox(checked = book.id in selected, onCheckedChange = { onClick(book) })
                        else IconButton(onClick = { onOptions(book) }) {
                            Icon(Icons.Rounded.MoreHoriz, "Opsi ${book.title}")
                        }
                    }
                }
                repeat(cols - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun FileArtwork(format: String, modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxWidth(.83f).fillMaxHeight(.94f)
            .clip(RoundedCornerShape(8.dp)).background(Color(0xFFF7F7F8))) {
            Icon(Icons.Rounded.Subject, null, modifier = Modifier.align(Alignment.TopCenter)
                .padding(top = 12.dp).size(32.dp), tint = Color(0xFFD0D0D6))
            Text(format.take(5).uppercase(Locale.ROOT), color = Color.White,
                fontSize = 10.sp, modifier = Modifier.align(Alignment.BottomStart)
                    .padding(start = 5.dp, bottom = 5.dp)
                    .clip(RoundedCornerShape(4.dp)).background(Blue)
                    .padding(horizontal = 4.dp, vertical = 2.dp))
        }
    }
}

@Composable
private fun SheetHeadline(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(40.dp))
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.titleLarge,
            maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}
@Composable
private fun SheetAction(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String,
    danger: Boolean = false, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 55.dp).clickable(onClick = onClick)
        .padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(25.dp),
            tint = if (danger) Color(0xFFFF5757) else Color.White)
        Spacer(Modifier.width(17.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge,
            color = if (danger) Color(0xFFFF5757) else Color.White,
            maxLines = 2, modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FolderEditorV2(value: String, onChange: (String) -> Unit, title: String,
    onDismiss: () -> Unit, onSave: (String) -> Unit) {
    val requester = remember { FocusRequester() }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Panel) {
        Column(Modifier.fillMaxWidth().imePadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(value = value, onValueChange = { onChange(it.take(60)) },
                label = { Text("Nama") }, singleLine = true,
                modifier = Modifier.fillMaxWidth().focusRequester(requester),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (value.isNotBlank()) onSave(value) }))
            LaunchedEffect(Unit) {
                // The sheet's content enters composition after its animation begins.
                // Focus only after the text field has attached to its FocusRequester.
                withFrameNanos { }
                requester.requestFocus()
            }
            Button(onClick = { onSave(value) }, enabled = value.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                modifier = Modifier.fillMaxWidth().height(58.dp)) {
                Text("Simpan", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
