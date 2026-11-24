package id.tuturium.app

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.background
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.unit.IntOffset
import android.content.ClipData
import android.content.ClipboardManager
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Genuine PDF page rendering. Text extraction is retained separately for narration. */
@Composable
internal fun OriginalPdfPages(
    source: File, readingPage: Int, follow: Boolean, reading: Boolean,
    firstPassByPage: Map<Int, Int>, passesByPage: Map<Int, List<IndexedValue<Pass>>>,
    onReadWord: (WordSelection) -> Unit
) {
    val session = remember(source.path) { PdfPageSession(source) }
    DisposableEffect(session) {
        onDispose { CoroutineScope(Dispatchers.IO).launch { session.close() } }
    }
    val count by produceState<Int?>(initialValue = null, key1 = session) {
        value = withContext(Dispatchers.IO) {
            runCatching { session.pageCount() }.getOrDefault(0)
        }
    }
    val total = count
    if (total == null || total <= 0) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (total == null) CircularProgressIndicator()
            else Text("Halaman PDF tidak dapat dibuka. Pilih mode Teks.",
                color = MaterialTheme.colorScheme.onBackground)
        }
        return
    }

    val target = readingPage.coerceIn(0, total - 1)
    val list = rememberLazyListState(initialFirstVisibleItemIndex = target)
    val scope = rememberCoroutineScope()
    var browsing by rememberSaveable(source.path) { mutableStateOf(false) }
    var requestedPage by remember { mutableStateOf<Int?>(null) }
    val dragging by list.interactionSource.collectIsDraggedAsState()
    val scrollInput = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && available.y != 0f) {
                    browsing = true
                    requestedPage = null
                }
                return Offset.Zero
            }
        }
    }
    LaunchedEffect(dragging) { if (dragging) browsing = true }
    LaunchedEffect(target, follow, browsing, requestedPage) {
        if (requestedPage == target) {
            requestedPage = null
            browsing = false
        }
        if (follow && !browsing && list.firstVisibleItemIndex != target) list.scrollToItem(target)
    }
    val awayFromReading by remember(target, list) {
        derivedStateOf { list.layoutInfo.visibleItemsInfo.none { it.index == target } }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(state = list, modifier = Modifier.fillMaxSize().nestedScroll(scrollInput)
            .semantics { contentDescription = "Dokumen PDF" },
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 52.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(total, key = { it }) { index ->
                val pass = firstPassByPage[index]
                PdfPageImage(session, source, index, passesByPage[index].orEmpty(), onReadWord, onRead = pass
                    ?.takeUnless { reading && index == target }
                    ?.let { start ->
                        {
                            browsing = true
                            requestedPage = index
                            onReadWord(WordSelection(start, 0))
                        }
                    })
            }
        }
        if (reading && awayFromReading && (browsing || !follow)) {
            FilledTonalIconButton(onClick = {
                requestedPage = null
                browsing = false
                scope.launch { list.scrollToItem(target) }
            }, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)) {
                Icon(Icons.Rounded.MyLocation, "Kembali ke halaman bacaan")
            }
        }
    }
}

@Composable
private fun PdfPageImage(session: PdfPageSession, source: File, index: Int,
    passes: List<IndexedValue<Pass>>, onReadWord: (WordSelection) -> Unit, onRead: (() -> Unit)?) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val read by rememberUpdatedState(onReadWord)
    val readPage by rememberUpdatedState(onRead)
    var selected by remember { mutableStateOf<PdfWordHit?>(null) }
    var anchor by remember { mutableStateOf(Offset.Zero) }
    var selectionJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val colors = MaterialTheme.colorScheme
    val dark = colors.background.luminance() < 0.4f
    val filter = remember(dark) {
        if (dark) ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
            -0.85f,0f,0f,0f,230f, 0f,-0.85f,0f,0f,230f,
            0f,0f,-0.85f,0f,230f, 0f,0f,0f,1f,0f))) else null
    }
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val pixelWidth = with(density) { (configuration.screenWidthDp.dp.toPx() * 1.6f)
        .toInt().coerceIn(720, 1600) }
    val result by produceState<Pair<Bitmap?, Boolean>>(null to false,
        session, index, pixelWidth) {
        value = withContext(Dispatchers.IO) {
            runCatching { session.render(index, pixelWidth) }.getOrNull() to true
        }
    }
    val bitmap = result.first
    val ratio = bitmap?.let { it.width.toFloat() / it.height } ?: session.cachedRatio(index)
    Box(Modifier.fillMaxWidth().aspectRatio(ratio.coerceIn(0.25f, 3f)),
        contentAlignment = Alignment.Center) {
        if (bitmap != null) {
            val image = remember(bitmap) { bitmap.asImageBitmap() }
            val action = if (onRead == null) Modifier else Modifier.semantics {
                onClick(label = "Baca halaman ini") { readPage?.invoke(); true }
            }
            Image(image, "Halaman PDF ${index + 1}", modifier = Modifier.fillMaxSize().then(action)
                .pointerInput(source, index, passes) {
                    detectTapGestures(onTap = { readPage?.invoke() }, onLongPress = { point ->
                        val x = point.x / size.width
                        val y = point.y / size.height
                        anchor = point
                        selectionJob?.cancel()
                        selectionJob = scope.launch {
                            val hit = withContext(Dispatchers.IO) {
                                runCatching { readPdfWordHits(context, source, index, passes) }
                                    .getOrDefault(emptyList())
                                    .filter { x >= it.left - 0.006f && x <= it.right + 0.006f &&
                                        y >= it.top - 0.008f && y <= it.bottom + 0.008f }
                                    .minByOrNull { kotlin.math.abs(x - (it.left + it.right) / 2f) +
                                        kotlin.math.abs(y - (it.top + it.bottom) / 2f) }
                            }
                            selected = hit
                            if (hit == null) android.widget.Toast.makeText(context,
                                "Teks tidak dapat dipilih pada lokasi ini. Gunakan mode Teks.",
                                android.widget.Toast.LENGTH_SHORT).show()
                        }
                    })
                }, contentScale = ContentScale.Fit, colorFilter = filter)
            Box(Modifier.align(Alignment.TopStart).offset {
                IntOffset(anchor.x.roundToInt(), anchor.y.roundToInt())
            }) {
                DropdownMenu(expanded = selected != null, onDismissRequest = { selected = null }) {
                    DropdownMenuItem(text = { Text("Baca") }, enabled = selected?.selection != null,
                        onClick = { selected?.selection?.let(read); selected = null })
                    DropdownMenuItem(text = { Text("Salin") }, onClick = {
                        selected?.let { hit ->
                            context.getSystemService(ClipboardManager::class.java)
                                .setPrimaryClip(ClipData.newPlainText("Teks PDF", hit.text))
                        }
                        selected = null
                    })
                }
            }
        } else Text(if (result.second) "Halaman PDF gagal dirender."
            else "Memuat halaman", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private class PdfPageSession(private val source: File) {
    private var renderer: PdfRenderer? = null
    private var closed = false
    private val ratios = java.util.concurrent.ConcurrentHashMap<Int, Float>()

    fun cachedRatio(index: Int): Float = ratios[index] ?: 0.707f
    private val pages = object : LruCache<Pair<Int, Int>, Bitmap>(24 * 1024 * 1024) {
        override fun sizeOf(key: Pair<Int, Int>, value: Bitmap) = value.allocationByteCount
    }

    @Synchronized fun pageCount(): Int = open().pageCount

    @Synchronized fun render(index: Int, width: Int): Bitmap {
        val key = index to width
        pages.get(key)?.let { return it }
        return open().openPage(index).use { page ->
            ratios[index] = page.width.toFloat() / page.height
            val scale = minOf(width.toFloat() / page.width, 3500f / page.height)
            val bitmapWidth = (page.width * scale).toInt().coerceAtLeast(1)
            val bitmapHeight = (page.height * scale).toInt().coerceAtLeast(1)
            val image = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
            try {
                image.eraseColor(android.graphics.Color.WHITE)
                page.render(image, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                pages.put(key, image)
                image
            } catch (error: Exception) {
                image.recycle()
                throw error
            }
        }
    }

    @Synchronized fun close() {
        closed = true
        pages.evictAll()
        renderer?.close()
        renderer = null
    }

    private fun open(): PdfRenderer {
        check(!closed)
        renderer?.let { return it }
        val descriptor = ParcelFileDescriptor.open(source, ParcelFileDescriptor.MODE_READ_ONLY)
        return try {
            PdfRenderer(descriptor).also { renderer = it }
        } catch (error: Exception) {
            descriptor.close()
            throw error
        }
    }
}
