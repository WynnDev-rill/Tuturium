package id.tuturium.app

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private data class ReaderRow(val heading: String?, val text: String,
    val passes: List<IndexedValue<Pass>>, val paragraphEnd: Boolean = false)

@Composable
fun Reader(model: TuturiumModel, book: Book, document: ReadingDocument, showSettings: () -> Unit) {
    val playback by ReadingService.state.collectAsStateWithLifecycle()
    key(book.id) {
        ReaderContent(model, book, document, playback, showSettings)
    }
}

@Composable
private fun ReaderContent(model: TuturiumModel, book: Book, document: ReadingDocument, playback: Playback,
    showSettings: () -> Unit) {
    val context = LocalContext.current
    val pdfSource = remember(book.id) {
        if (book.format == "PDF") Library.get(context).originalPdf(book.id) else null
    }
    var showPdf by rememberSaveable(book.id) { mutableStateOf(pdfSource != null) }
    val active = playback.bookId == book.id && playback.total > 0
    val current = (if (active) playback.index else model.position).coerceIn(0, document.passes.lastIndex)
    val pdfPasses = remember(document) {
        if (pdfSource != null) document.firstPassByPdfPage() else emptyMap()
    }
    val isPlaying = active && playback.playing
    val isLoading = playback.bookId == book.id && playback.loading
    var panel by remember { mutableStateOf<String?>(null) }
    var jump by remember { mutableIntStateOf(-1) }
    var query by rememberSaveable { mutableStateOf("") }
    var matchIndex by remember { mutableIntStateOf(0) }
    var matches by remember { mutableStateOf(emptyList<ReaderMatch>()) }
    val searchOpen = panel == "search"
    BackHandler(searchOpen) { panel = null }
    var preview by remember { mutableStateOf<Float?>(null) }
    var clock by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    val marked = model.bookmarks.any { it.index == current }
    val offlineVoice = OfflineCatalog.voice(model.settings.voice)
    LaunchedEffect(book.id, offlineVoice?.model) {
        if (offlineVoice != null) {
            delay(350)
            val state = ReadingService.state.value
            if (!state.playing && !state.loading) OfflineSynth.prewarm(context.applicationContext)
        }
    }
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
                val paragraphs = document.paragraphs[sectionIndex]
                val passes = indexed[sectionIndex].orEmpty().groupBy { it.value.paragraph }
                paragraphs.forEachIndexed { paragraph, text ->
                    val phrases = passes[paragraph].orEmpty()
                    var first = 0
                    while (first < phrases.size) {
                        val start = if (first == 0) 0 else phrases[first].value.offset
                        var last = first + 1
                        // Bound the size of a layout row while keeping each spoken phrase intact.
                        while (last < phrases.size && phrases[last].value.offset + phrases[last].value.text.length - start <= 1280) {
                            last++
                        }
                        val end = phrases.getOrNull(last)?.value?.offset ?: text.length
                        val rowPasses = phrases.subList(first, last).map {
                            IndexedValue(it.index, it.value.copy(offset = it.value.offset - start))
                        }
                        add(ReaderRow(null, text.substring(start, end), rowPasses, last == phrases.size))
                        first = last
                    }
                }
            }
        }
    }
    LaunchedEffect(query, rows) {
        matches = withContext(Dispatchers.Default) { findReaderMatches(rows.map { it.text }, query) }
        matchIndex = 0
    }
    val searchMatch = matches.getOrNull(matchIndex).takeIf { searchOpen }
    val locations = remember(rows, document) {
        IntArray(document.passes.size).also { indices ->
            rows.forEachIndexed { row, item -> item.passes.forEach { indices[it.index] = row } }
        }
    }
    val sectionStarts = remember(document) {
        document.passes.withIndex().distinctBy { it.value.section }
    }
    val list = rememberLazyListState(initialFirstVisibleItemIndex = locations[current])
    var manualBrowsing by remember(book.id) { mutableStateOf(false) }
    val dragging by list.interactionSource.collectIsDraggedAsState()
    LaunchedEffect(dragging) {
        if (dragging) manualBrowsing = true
    }
    var activeWordOffScreen by remember { mutableStateOf(false) }
    var activeBelowViewport by remember { mutableStateOf(false) }
    val readingOffScreen by remember(list, locations, current) {
        derivedStateOf {
            list.layoutInfo.visibleItemsInfo.isNotEmpty() &&
                (activeWordOffScreen || list.layoutInfo.visibleItemsInfo.none { it.index == locations[current] })
        }
    }
    LaunchedEffect(searchMatch, showPdf) {
        if (searchMatch != null && !showPdf) {
            manualBrowsing = true
            if (list.layoutInfo.visibleItemsInfo.none { it.index == searchMatch.row })
                list.scrollToItem(searchMatch.row)
        }
    }
    LaunchedEffect(current, model.follow, jump, manualBrowsing, showPdf, searchOpen) {
        if (!showPdf && !searchOpen && (jump == current ||
            (model.follow && !manualBrowsing && (isPlaying || current != 0)))) {
            val target = locations[current]
            val info = list.layoutInfo
            val visible = info.visibleItemsInfo.firstOrNull { it.index == target }
            if (jump == current || visible == null) {
                list.scrollToItem(target)
            }
            if (jump == current) jump = -1
        }
    }
    fun seek(index: Int) {
        val target = index.coerceIn(0, document.passes.lastIndex)
        jump = target
        manualBrowsing = false
        model.seek(target)
        panel = null
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = model::closeReader, enabled = !model.savingReader) {
                if (model.savingReader) CircularProgressIndicator(Modifier.size(23.dp), strokeWidth = 2.dp)
                else Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Kembali ke pustaka", Modifier.size(28.dp))
            }
            Text(book.title, modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            IconButton(onClick = { model.toggleBookmark(current) }) {
                Icon(if (marked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                    if (marked) "Hapus markah" else "Simpan markah",
                    tint = if (marked) Color(0xFFF23E3E) else MaterialTheme.colorScheme.onBackground)
            }
            IconButton(onClick = { panel = "menu" }) {
                Icon(Icons.Rounded.MoreHoriz, "Opsi pembaca", Modifier.size(27.dp))
            }
        }
        if (book.format == "PDF" && pdfSource != null) {
            Row(Modifier.fillMaxWidth().padding(bottom = 11.dp),
                horizontalArrangement = Arrangement.Center) {
                Surface(shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Row(Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        listOf(true to "PDF", false to "Teks").forEach { (pdf, label) ->
                            val selected = showPdf == pdf
                            Surface(onClick = {
                                showPdf = pdf
                                if (!pdf) {
                                    manualBrowsing = false
                                    jump = current
                                }
                            }, shape = RoundedCornerShape(20.dp),
                                color = if (selected) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent) {
                                Text(label, modifier = Modifier.padding(horizontal = 22.dp, vertical = 9.dp),
                                    fontSize = 14.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
        if ((active || book.position > 0 || book.wordOffset > 0) && !showPdf &&
            manualBrowsing && readingOffScreen
        ) {
            val spokenText = document.passes.getOrNull(current)?.text.orEmpty()
            val range = playback.range.takeIf { active }
            val snippet = if (range != null && range.first in spokenText.indices) {
                spokenText.substring(range.first, (range.last + 1).coerceAtMost(spokenText.length))
            } else {
                spokenText.trimStart().substringBefore(' ').take(28)
            }
            Row(Modifier.fillMaxWidth().padding(bottom = 9.dp),
                horizontalArrangement = Arrangement.Center) {
                Surface(onClick = {
                    showPdf = false
                    if (searchOpen) panel = null
                    manualBrowsing = false
                    jump = current
                }, modifier = Modifier.semantics { contentDescription = "Kembali ke kalimat yang dibacakan" },
                    shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                    Row(Modifier.padding(horizontal = 15.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        val first = list.firstVisibleItemIndex
                        val below = locations[current] > first ||
                            (locations[current] == first && activeBelowViewport)
                        Icon(if (below) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowUp,
                            null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(6.dp))
                        Text(snippet.take(30).ifBlank { "Kembali ke bacaan" },
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
        if (showPdf && pdfSource != null) {
            Box(Modifier.weight(1f)) {
                OriginalPdfPages(pdfSource, document.pdfPageForPass(current), model.follow,
                    reading = isPlaying || isLoading, firstPassByPage = pdfPasses,
                    onReadPage = { model.playFromWord(book, WordSelection(it, 0)) })
            }
        } else {
        val scheme = MaterialTheme.colorScheme
        val warm = model.readerPreset == "warm"
        val focusMode = model.readerPreset == "focus"
        val dark = scheme.background.luminance() < 0.4f
        val paper = if (!warm) scheme.background else if (dark) Color(0xFF28211B) else Color(0xFFFFF2DB)
        val ink = if (!warm) scheme.onBackground else if (dark) Color(0xFFF7E6D4) else Color(0xFF382719)
        LazyColumn(state = list, modifier = Modifier.weight(1f).fillMaxWidth()
            .background(paper),
            contentPadding = PaddingValues(horizontal = if (focusMode) 38.dp else 24.dp, vertical = 17.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)) {
            itemsIndexed(rows) { rowIndex, row ->
                if (row.heading != null) {
                    if (book.format == "PDF") {
                        Text(row.heading, modifier = Modifier.padding(top = 18.dp, bottom = 17.dp),
                            fontFamily = FontFamily.SansSerif, fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium)
                    } else {
                        Text(row.heading, style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    val pass = row.passes.firstOrNull { it.index == current }?.value
                    val wordColor = readerCursorColor(model.readerHighlight, MaterialTheme.colorScheme.primary)
                    val sentenceColor = if (model.readerHighlight == "theme") MaterialTheme.colorScheme.primaryContainer
                        else wordColor.copy(alpha = if (dark) 0.22f else 0.34f)
                    val highlightSentence = model.readerHighlightSentence
                    val wordInk = if (model.readerHighlight == "theme") MaterialTheme.colorScheme.onPrimary
                        else Color(0xFF202020)
                    val resultRange = searchMatch?.takeIf { it.row == rowIndex }?.range
                    val spokenRange = if (active && pass != null) playback.range
                        else if (!active && pass != null && current == book.position && book.wordOffset in pass.text.indices)
                            book.wordOffset..book.wordOffset
                        else null
                    val highlighted = remember(row, pass, spokenRange, sentenceColor, wordColor, wordInk, resultRange, highlightSentence) {
                        buildAnnotatedString {
                            append(row.text)
                            if (pass != null) {
                                val end = (pass.offset + pass.text.length).coerceAtMost(length)
                                if (highlightSentence) addStyle(SpanStyle(background = sentenceColor), pass.offset, end)
                                if (spokenRange != null && spokenRange.first in pass.text.indices) {
                                    val start = pass.offset + spokenRange.first
                                    val wordEnd = minOf(end, pass.offset + spokenRange.last + 1)
                                    if (wordEnd > start) addStyle(
                                        SpanStyle(background = wordColor, color = wordInk, fontWeight = FontWeight.SemiBold),
                                        start, wordEnd
                                    )
                                }
                            }
                            if (resultRange != null) addStyle(SpanStyle(background = Color(0xFFFFBC68),
                                color = Color(0xFF211800)), resultRange.first, resultRange.last + 1)
                        }
                    }
                    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
                    LaunchedEffect(resultRange, layout) {
                        val result = resultRange ?: return@LaunchedEffect
                        val bounds = layout?.getBoundingBox(result.first) ?: return@LaunchedEffect
                        val item = snapshotFlow {
                            list.layoutInfo.visibleItemsInfo.firstOrNull { it.index == rowIndex }
                        }.filterNotNull().first()
                        val info = list.layoutInfo
                        val center = item.offset + bounds.center.y
                        val middle = (info.viewportStartOffset + info.viewportEndOffset) / 2f
                        list.scrollBy(center - middle)
                    }
                    LaunchedEffect(pass, spokenRange, layout, model.follow, isPlaying, manualBrowsing, dragging, searchOpen) {
                        if (searchOpen) return@LaunchedEffect
                        val textLayout = layout ?: return@LaunchedEffect
                        val selected = pass ?: return@LaunchedEffect
                        val word = (selected.offset + (spokenRange?.first ?: 0)).coerceIn(row.text.indices)
                        val bounds = textLayout.getBoundingBox(word)
                        if (manualBrowsing) {
                            snapshotFlow {
                                val info = list.layoutInfo
                                val item = info.visibleItemsInfo.firstOrNull { it.index == rowIndex }
                                val visible = item != null && item.offset + bounds.bottom > info.viewportStartOffset &&
                                    item.offset + bounds.top < info.viewportEndOffset
                                Triple(visible, list.isScrollInProgress,
                                    item != null && item.offset + bounds.top >= info.viewportEndOffset)
                            }.collect { (visible, scrolling, below) ->
                                activeWordOffScreen = !visible
                                activeBelowViewport = below
                                if (visible && !scrolling && !dragging) manualBrowsing = false
                            }
                        } else {
                            activeWordOffScreen = false
                            if (model.follow && !dragging && (isPlaying || spokenRange != null)) {
                                val info = list.layoutInfo
                                val item = info.visibleItemsInfo.firstOrNull { it.index == rowIndex } ?: return@LaunchedEffect
                                val center = item.offset + (bounds.top + bounds.bottom) / 2f
                                // Keep the current line comfortably in view without moving on each word.
                                val comfortZone = (info.viewportEndOffset - info.viewportStartOffset) * 0.20f
                                val delta = readerCenterDelta(center, info.viewportStartOffset,
                                    info.viewportEndOffset, comfortZone, false)
                                if (delta != 0f) list.scrollBy(delta)
                            }
                        }
                    }
                    val latestPlay by rememberUpdatedState<(WordSelection) -> Unit>({
                        manualBrowsing = false
                        jump = it.passIndex
                        if (!model.savingReader) model.playFromWord(book, it)
                    })
                    val readableSize = model.fontSize.coerceIn(16f, 34f)
                    Text(highlighted, modifier = Modifier.fillMaxWidth()
                        .padding(bottom = if (row.paragraphEnd) 17.dp else 0.dp)
                        .pointerInput(row) {
                        detectTapGestures { point ->
                            val offset = layout?.getOffsetForPosition(point) ?: return@detectTapGestures
                            selectSpokenWord(row.text, row.passes, offset)?.let(latestPlay)
                        }
                    }, onTextLayout = { layout = it },
                        fontFamily = readerTypeface(model.readerFont),
                        fontWeight = if (model.readerBold) FontWeight.Bold else FontWeight.Normal,
                        fontSize = readableSize.sp,
                        lineHeight = (readableSize * if (focusMode) 1.66f else 1.48f).sp,
                        textAlign = TextAlign.Start,
                        color = ink)
                }
            }
        }
        }
        if (searchOpen) ReaderSearchBar(query, {
            query = it
            matches = emptyList()
            matchIndex = 0
        }, matchIndex, matches.size, { delta ->
            if (matches.isNotEmpty()) matchIndex = (matchIndex + delta + matches.size) % matches.size
        }, { panel = null })
        else ReaderPlaybackBar(model, book, document, playback, current, isPlaying, isLoading,
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
        "menu" -> ReaderMenuSheet(model, book, current, marked,
            dismiss = { panel = null }, navigate = {
                if (it == "search") showPdf = false
                panel = it
            })
        "voices" -> ReaderVoiceSheet(model) { panel = null }
        "timer" -> ReaderTimerSheet(playback) { panel = null }
        "speed" -> SpeedControlSheet(model, playback) { panel = null }
        "queue" -> ReaderQueueSheet(model) { panel = null }
        "notes" -> ReaderNotesSheet(book, current) { panel = null }
        "skip" -> SkipOptionsSheet(model) { panel = null }
        "rename" -> RenameBookSheet(book.title, { panel = null }) {
            model.rename(book, it)
            panel = null
        }
        "display" -> ReaderAppearanceSheet(model) { panel = null }
        "audio" -> ReaderAudioSheet(model) { panel = null }
        "delete" -> TuturiumDialog(onDismissRequest = { panel = null },
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
    TuturiumDialog(onDismissRequest = dismiss, title = { Text(title) },
        text = { LazyColumn(Modifier.fillMaxWidth().heightIn(max = 360.dp), content = content) },
        confirmButton = { TextButton(onClick = dismiss) { Text("Tutup") } })
}

internal fun rate(value: Float) = String.format(Locale.forLanguageTag("id-ID"), "%.2f×", value)

internal fun readerCenterDelta(wordCenter: Float, start: Int, end: Int, tolerance: Float,
    manualBrowsing: Boolean): Float {
    if (manualBrowsing || end <= start) return 0f
    val delta = wordCenter - (start + end) / 2f
    return if (kotlin.math.abs(delta) > tolerance) delta else 0f
}
