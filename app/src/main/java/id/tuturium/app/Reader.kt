package id.tuturium.app

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlinx.coroutines.delay

private data class ReaderRow(val heading: String?, val text: String, val passes: List<IndexedValue<Pass>>)

@Composable
fun Reader(model: TuturiumModel, book: Book, document: ReadingDocument, playback: Playback, showSettings: () -> Unit) {
    key(book.id) {
        ReaderContent(model, book, document, playback, showSettings)
    }
}

@Composable
private fun ReaderContent(model: TuturiumModel, book: Book, document: ReadingDocument, playback: Playback,
    showSettings: () -> Unit) {
    val context = LocalContext.current
    val active = playback.bookId == book.id && playback.total > 0
    val current = (if (active) playback.index else model.position).coerceIn(0, document.passes.lastIndex)
    val isPlaying = active && playback.playing
    val isLoading = playback.bookId == book.id && playback.loading
    var panel by remember { mutableStateOf<String?>(null) }
    var jump by remember { mutableIntStateOf(-1) }
    var query by remember { mutableStateOf("") }
    var preview by remember { mutableStateOf<Float?>(null) }
    var clock by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    val marked = model.bookmarks.any { it.index == current }
    LaunchedEffect(book.id, model.listeningOptions.autoplay) {
        if (model.listeningOptions.autoplay && ReadingService.state.value.bookId != book.id)
            model.play(book, current)
    }
    LaunchedEffect(playback.timerUntil) {
        while (playback.timerUntil > 0) {
            clock = SystemClock.elapsedRealtime()
            delay(1000)
        }
    }

    val rows = remember(document) {
        val indexed = document.passes.withIndex().groupBy { it.value.section }
        buildList {
            document.sections.forEachIndexed { sectionIndex, section ->
                if (document.sections.size > 1) add(ReaderRow(section.title, "", emptyList()))
                val paragraphs = section.text.split(Regex("\\n+")).filter { it.isNotBlank() }
                val passes = indexed[sectionIndex].orEmpty().groupBy { it.value.paragraph }
                paragraphs.forEachIndexed { paragraph, text ->
                    val phrases = passes[paragraph].orEmpty()
                    var first = 0
                    while (first < phrases.size) {
                        val start = if (first == 0) 0 else phrases[first].value.offset
                        var last = first + 1
                        // Bound the size of a layout row while keeping each spoken phrase intact.
                        while (last < phrases.size && phrases[last].value.offset + phrases[last].value.text.length - start <= 640) {
                            last++
                        }
                        val end = phrases.getOrNull(last)?.value?.offset ?: text.length
                        val rowPasses = phrases.subList(first, last).map {
                            IndexedValue(it.index, it.value.copy(offset = it.value.offset - start))
                        }
                        add(ReaderRow(null, text.substring(start, end), rowPasses))
                        first = last
                    }
                }
            }
        }
    }
    val locations = remember(rows, document) {
        IntArray(document.passes.size).also { indices ->
            rows.forEachIndexed { row, item -> item.passes.forEach { indices[it.index] = row } }
        }
    }
    val sectionStarts = remember(document) {
        document.passes.withIndex().distinctBy { it.value.section }
    }
    val list = rememberLazyListState(initialFirstVisibleItemIndex = locations[current])
    LaunchedEffect(current, model.follow, jump) {
        if (jump == current || (model.follow && (isPlaying || current != 0))) {
            val target = locations[current]
            val info = list.layoutInfo
            val visible = info.visibleItemsInfo.firstOrNull { it.index == target }
            if (jump == current || visible == null || visible.offset < 0 ||
                visible.offset + minOf(visible.size, 140) > info.viewportEndOffset) {
                list.scrollToItem(target)
            }
            if (jump == current) jump = -1
        }
    }
    fun seek(index: Int) {
        val target = index.coerceIn(0, document.passes.lastIndex)
        jump = target
        model.seek(target)
        panel = null
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = model::closeReader) {
                Icon(Icons.Rounded.KeyboardArrowDown, "Kembali ke pustaka", Modifier.size(31.dp))
            }
            Text(book.title, modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            IconButton(onClick = { model.toggleBookmark(current) }) {
                Icon(if (marked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                    if (marked) "Hapus markah" else "Simpan markah",
                    tint = if (marked) Color(0xFFF23E3E) else Color.White)
            }
            IconButton(onClick = { panel = "menu" }) {
                Icon(Icons.Rounded.MoreHoriz, "Opsi pembaca", Modifier.size(27.dp))
            }
        }
        LazyColumn(state = list, modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)) {
            itemsIndexed(rows) { _, row ->
                if (row.heading != null) {
                    Text(row.heading, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                } else {
                    val pass = row.passes.firstOrNull { it.index == current }?.value
                    val sentenceColor = MaterialTheme.colorScheme.primaryContainer
                    val wordColor = MaterialTheme.colorScheme.primary
                    val wordInk = MaterialTheme.colorScheme.onPrimary
                    val spokenRange = playback.range.takeIf { active && pass != null }
                    val highlighted = remember(row, pass, spokenRange, sentenceColor, wordColor, wordInk) {
                        buildAnnotatedString {
                            append(row.text)
                            if (pass != null) {
                                val end = (pass.offset + pass.text.length).coerceAtMost(length)
                                addStyle(SpanStyle(background = sentenceColor), pass.offset, end)
                                if (spokenRange != null && spokenRange.first in pass.text.indices) {
                                    val start = pass.offset + spokenRange.first
                                    val wordEnd = minOf(end, pass.offset + spokenRange.last + 1)
                                    if (wordEnd > start) addStyle(
                                        SpanStyle(background = wordColor, color = wordInk, fontWeight = FontWeight.SemiBold),
                                        start, wordEnd
                                    )
                                }
                            }
                        }
                    }
                    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
                    val bringIntoView = remember { BringIntoViewRequester() }
                    LaunchedEffect(spokenRange, layout, model.follow, isPlaying) {
                        val word = spokenRange?.first?.plus(pass?.offset ?: 0)
                        val textLayout = layout
                        if (model.follow && isPlaying && word != null && word in row.text.indices && textLayout != null) {
                            bringIntoView.bringIntoView(textLayout.getBoundingBox(word))
                        }
                    }
                    val latestSeek by rememberUpdatedState<(Int) -> Unit>({ seek(it) })
                    Text(highlighted, modifier = Modifier.fillMaxWidth().bringIntoViewRequester(bringIntoView).pointerInput(row) {
                        detectTapGestures { point ->
                            val offset = layout?.getOffsetForPosition(point) ?: return@detectTapGestures
                            row.passes.lastOrNull { it.value.offset <= offset }?.index?.let(latestSeek)
                        }
                    }, onTextLayout = { layout = it }, fontFamily = ReaderFont, fontSize = model.fontSize.sp,
                        lineHeight = (model.fontSize * 1.48f).sp, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
        ReaderPlaybackBar(model, book, document, playback, current, isPlaying, isLoading,
            speedPanel = { panel = "speed" }, voicePanel = { panel = "voices" }, seek = ::seek)
    }

    when (panel) {
        "sections" -> ReaderOptions("Daftar isi", { panel = null }) {
            itemsIndexed(sectionStarts) { _, item ->
                ListItem(headlineContent = { Text(document.sections[item.value.section].title) },
                    modifier = Modifier.clickable { seek(item.index) })
            }
        }
        "bookmarks" -> ReaderOptions("Markah", { panel = null }) {
            if (model.bookmarks.isEmpty()) item { Text("Belum ada markah.", Modifier.padding(12.dp)) }
            itemsIndexed(model.bookmarks) { _, mark ->
                ListItem(headlineContent = { Text(mark.excerpt) }, modifier = Modifier.clickable { seek(mark.index) },
                    trailingContent = { IconButton({ model.toggleBookmark(mark.index) }) {
                        Icon(Icons.Rounded.DeleteOutline, "Hapus markah")
                    } })
            }
        }
        "search" -> {
            val matches = remember(query, document) {
                if (query.isBlank()) emptyList()
                else document.passes.withIndex().asSequence().filter { it.value.text.contains(query.trim(), true) }.take(100).toList()
            }
            ReaderOptions("Cari di dokumen", { panel = null }) {
                item {
                    OutlinedTextField(query, { query = it }, singleLine = true, label = { Text("Kata atau frasa") },
                        modifier = Modifier.fillMaxWidth())
                }
                if (query.isNotBlank() && matches.isEmpty()) item { Text("Tidak ditemukan.", Modifier.padding(12.dp)) }
                itemsIndexed(matches) { _, match ->
                    ListItem(headlineContent = { Text(match.value.text) }, modifier = Modifier.clickable { seek(match.index) })
                }
            }
        }
        "menu" -> ReaderMenuSheet(model, book, current, marked,
            dismiss = { panel = null }, navigate = { panel = it })
        "voices" -> ReaderVoiceSheet(model) { panel = null }
        "timer" -> ReaderTimerSheet(playback) { panel = null }
        "speed" -> SpeedControlSheet(model, playback) { panel = null }
        "queue" -> ReaderQueueSheet(model) { panel = null }
        "notes" -> ReaderNotesSheet(book, current) { panel = null }
        "skip" -> SkipOptionsSheet(model) { panel = null }
        "rename" -> {
            var newTitle by remember(book.id) { mutableStateOf(book.title) }
            AlertDialog(onDismissRequest = { panel = null },
                title = { Text("Ubah nama") },
                text = { OutlinedTextField(newTitle, { newTitle = it.take(200) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true) },
                confirmButton = { TextButton(onClick = {
                    model.rename(book, newTitle); panel = null
                }, enabled = newTitle.isNotBlank()) { Text("Simpan") } },
                dismissButton = { TextButton(onClick = { panel = null }) { Text("Batal") } })
        }
        "display" -> AlertDialog(onDismissRequest = { panel = null },
            title = { Text("Tampilan") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    VoiceSlider("Ukuran teks", "${model.fontSize.toInt()} sp", model.fontSize,
                        16f..34f, model::changeFont)
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        listOf("system" to "Sistem", "light" to "Terang",
                            "dark" to "Gelap").forEach { (value, label) ->
                            FilterChip(model.theme == value, { model.changeTheme(value) },
                                label = { Text(label) })
                        }
                    }
                }
            }, confirmButton = { TextButton(onClick = { panel = null }) { Text("Selesai") } })
        "delete" -> AlertDialog(onDismissRequest = { panel = null },
            title = { Text("Hapus file?") },
            text = { Text("Dokumen dipindahkan ke File yang Dihapus.") },
            confirmButton = { TextButton(onClick = {
                model.remove(book); model.closeReader(); panel = null
            }) { Text("Hapus") } },
            dismissButton = { TextButton(onClick = { panel = null }) { Text("Batal") } })
    }
}

@Composable
private fun ReaderOptions(title: String, dismiss: () -> Unit, content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    AlertDialog(onDismissRequest = dismiss, title = { Text(title) },
        text = { LazyColumn(Modifier.fillMaxWidth().heightIn(max = 360.dp), content = content) },
        confirmButton = { TextButton(onClick = dismiss) { Text("Tutup") } })
}

internal fun rate(value: Float) = String.format(Locale.forLanguageTag("id-ID"), "%.2f×", value)
