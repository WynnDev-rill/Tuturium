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
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(model::closeReader) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Kembali ke pustaka") }
            Column(Modifier.weight(1f)) {
                Text(book.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(document.sections[document.passes[current].section].title,
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton({ model.toggleBookmark(current) }) {
                Icon(if (marked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                    if (marked) "Hapus markah" else "Simpan markah", tint = MaterialTheme.colorScheme.primary)
            }
            Box {
                var menu by remember { mutableStateOf(false) }
                IconButton({ menu = true }) { Icon(Icons.Rounded.MoreVert, "Opsi pembaca") }
                DropdownMenu(menu, { menu = false }) {
                    listOf("Daftar isi" to "sections", "Markah" to "bookmarks", "Cari di dokumen" to "search",
                        "Pengatur waktu tidur" to "timer", "Setelan suara" to "voice").forEach { (label, id) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = {
                            menu = false
                            if (id == "voice") { model.closeReader(); showSettings() } else panel = id
                        })
                    }
                }
            }
        }
        HorizontalDivider()
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
        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp,
            shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp)) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                if (document.passes.size > 1) {
                    Slider(value = preview ?: current.toFloat(), onValueChange = { preview = it },
                        onValueChangeFinished = { preview?.let { seek(it.toInt()) }; preview = null },
                        valueRange = 0f..document.passes.lastIndex.toFloat())
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("${(preview?.toInt() ?: current) + 1} / ${document.passes.size}",
                        style = MaterialTheme.typography.labelMedium)
                    TextButton({ panel = "speed" }) { Text(rate(model.settings.speed)) }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    IconButton({ seek(current - 1) }, enabled = current > 0) {
                        Icon(Icons.Rounded.SkipPrevious, "Kalimat sebelumnya")
                    }
                    FilledIconButton({ model.play(book, current) }, modifier = Modifier.size(62.dp)) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(23.dp), color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp)
                        else Icon(if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            if (isPlaying) "Jeda" else "Putar", Modifier.size(31.dp))
                    }
                    IconButton({ seek(current + 1) }, enabled = current < document.passes.lastIndex) {
                        Icon(Icons.Rounded.SkipNext, "Kalimat berikutnya")
                    }
                }
                if (playback.timerUntil > clock) Text(
                    "Jeda dalam ${(playback.timerUntil - clock + 59_999) / 60_000} mnt",
                    modifier = Modifier.clickable { panel = "timer" }.padding(8.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
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
        "timer" -> ReaderOptions("Pengatur waktu tidur", { panel = null }) {
            if (playback.bookId == null) item {
                Text("Mulai pemutaran untuk menggunakan pengatur waktu.", Modifier.padding(12.dp))
            } else itemsIndexed(listOf(0, 15, 30, 45, 60, 90)) { _, minutes ->
                ListItem(headlineContent = { Text(if (minutes == 0) "Matikan pengatur waktu" else "$minutes menit") },
                    modifier = Modifier.clickable {
                        ReadingService.command(context, ReadingService.TIMER, minutes = minutes)
                        panel = null
                    })
            }
        }
        "speed" -> AlertDialog(onDismissRequest = { panel = null }, title = { Text("Kecepatan suara") },
            text = { VoiceSlider("Kecepatan", rate(model.settings.speed), model.settings.speed, .5f..3f,
                { model.changeSettings(model.settings.copy(speed = it), false) },
                { model.changeSettings(model.settings) }) },
            confirmButton = { TextButton({ model.changeSettings(model.settings); panel = null }) { Text("Selesai") } })
    }
}

@Composable
private fun ReaderOptions(title: String, dismiss: () -> Unit, content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    AlertDialog(onDismissRequest = dismiss, title = { Text(title) },
        text = { LazyColumn(Modifier.fillMaxWidth().heightIn(max = 360.dp), content = content) },
        confirmButton = { TextButton(onClick = dismiss) { Text("Tutup") } })
}

internal fun rate(value: Float) = String.format(Locale.forLanguageTag("id-ID"), "%.2f×", value)
