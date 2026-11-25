package id.tuturium.app

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import java.io.File
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val edgeLabels = listOf("Kiri", "Atas", "Kanan", "Bawah")

@Composable
internal fun PdfMarginSheet(source: File, visiblePage: Int, saved: PdfMarginSettings,
    dismiss: () -> Unit, save: (PdfMarginSettings) -> Unit) {
    val session = remember(source.path) { PdfPageSession(source) }
    val count by produceState<Int?>(null, session) {
        value = withContext(Dispatchers.IO) { runCatching { session.pageCount() }.getOrDefault(0) }
    }
    DisposableEffect(session) {
        onDispose { CoroutineScope(Dispatchers.IO).launch { session.close() } }
    }
    var page by remember { mutableIntStateOf(visiblePage) }
    var all by remember { mutableStateOf(false) }
    var area by remember { mutableStateOf(saved.page(visiblePage)) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    var failed by remember { mutableStateOf(false) }
    LaunchedEffect(session, page, count) {
        if ((count ?: 0) <= 0) return@LaunchedEffect
        page = page.coerceIn(0, count!! - 1)
        preview = null
        failed = false
        try {
            preview = withContext(Dispatchers.IO) { session.render(page, 720) }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            failed = true
        }
    }

    fun changePage(next: Int) {
        page = next
        if (!all) area = saved.page(next)
    }

    TuturiumSheet(onDismissRequest = dismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Area bacaan PDF", style = MaterialTheme.typography.titleLarge)
            Text("Bagian yang diarsir dilewati saat dibacakan.", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !all, onClick = {
                    if (all) { all = false; area = saved.page(page) }
                }, label = { Text("Halaman ini") })
                FilterChip(selected = all, onClick = {
                    if (!all) { all = true; area = saved.all }
                }, label = { Text("Semua halaman") })
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { changePage(page - 1) }, enabled = page > 0) {
                    Icon(Icons.Rounded.ChevronLeft, "Halaman pratinjau sebelumnya")
                }
                Text("Pratinjau halaman ${page + 1}")
                IconButton(onClick = { changePage(page + 1) }, enabled = page + 1 < (count ?: 0)) {
                    Icon(Icons.Rounded.ChevronRight, "Halaman pratinjau berikutnya")
                }
            }
            val bitmap = preview
            if (bitmap == null) {
                Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    if (failed || count == 0) Text("Pratinjau halaman tidak tersedia.")
                    else CircularProgressIndicator()
                }
            } else {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(Modifier.widthIn(max = 330.dp)
                        .fillMaxWidth(.86f).aspectRatio(bitmap.width.toFloat() / bitmap.height)) {
                        Image(remember(bitmap) { bitmap.asImageBitmap() }, null,
                            Modifier.matchParentSize(), contentScale = ContentScale.FillBounds)
                        MarginOverlay(area, { area = it }, Modifier.matchParentSize())
                    }
                }
                val edges = listOf(area.left, area.top, 1f - area.right, 1f - area.bottom)
                for ((index, edge) in edges.withIndex()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(edgeLabels[index], Modifier.width(64.dp), style = MaterialTheme.typography.bodySmall)
                        Slider(value = edge, onValueChange = { fraction ->
                            area = area.move(index, if (index >= 2) 1f - fraction else fraction)
                        }, valueRange = 0f..0.90f, modifier = Modifier.weight(1f).semantics {
                            contentDescription = "Margin ${edgeLabels[index].lowercase()}"
                        })
                        Text("${(edge * 100).roundToInt()}%", Modifier.width(37.dp),
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { area = PdfMargins() }, modifier = Modifier.weight(1f)) { Text("Reset") }
                Button(enabled = bitmap != null, onClick = {
                    save(if (all) PdfMarginSettings(area) else saved.copy(pages =
                        if (area == saved.all) saved.pages - page else saved.pages + (page to area)))
                }, modifier = Modifier.weight(1f)) { Text("Simpan") }
            }
        }
    }
}

@Composable
private fun MarginOverlay(area: PdfMargins, onChange: (PdfMargins) -> Unit, modifier: Modifier) {
    val bounds by rememberUpdatedState(area)
    val change by rememberUpdatedState(onChange)
    val stroke = MaterialTheme.colorScheme.primary
    Canvas(modifier.pointerInput(Unit) {
        var edge = 0
        detectDragGestures(onDragStart = { start ->
            val box = bounds
            val distances = listOf(
                abs(start.x - box.left * size.width), abs(start.y - box.top * size.height),
                abs(start.x - box.right * size.width), abs(start.y - box.bottom * size.height)
            )
            edge = distances.indices.minBy { distances[it] }
        }) { event, _ ->
            event.consume()
            val relative = if (edge % 2 == 0) event.position.x / size.width else event.position.y / size.height
            change(bounds.move(edge, relative))
        }
    }) {
        val left = area.left * size.width
        val right = area.right * size.width
        val top = area.top * size.height
        val bottom = area.bottom * size.height
        val shade = Color.Black.copy(alpha = .52f)
        drawRect(shade, size = Size(size.width, top))
        drawRect(shade, Offset(0f, bottom), Size(size.width, size.height - bottom))
        drawRect(shade, Offset(0f, top), Size(left, bottom - top))
        drawRect(shade, Offset(right, top), Size(size.width - right, bottom - top))
        val width = 2.dp.toPx()
        drawLine(stroke, Offset(left, top), Offset(right, top), width)
        drawLine(stroke, Offset(left, bottom), Offset(right, bottom), width)
        drawLine(stroke, Offset(left, top), Offset(left, bottom), width)
        drawLine(stroke, Offset(right, top), Offset(right, bottom), width)
        val middleX = (left + right) / 2
        val middleY = (top + bottom) / 2
        for (handle in listOf(Offset(left, middleY), Offset(right, middleY),
            Offset(middleX, top), Offset(middleX, bottom))) drawCircle(stroke, 5.dp.toPx(), handle)
    }
}
