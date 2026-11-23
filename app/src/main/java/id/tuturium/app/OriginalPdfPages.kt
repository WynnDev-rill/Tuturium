package id.tuturium.app

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Genuine PDF page rendering. Text extraction is retained separately for narration. */
@Composable
fun OriginalPdfPages(source: File) {
    val count by produceState<Int?>(initialValue = null, key1 = source.path) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                ParcelFileDescriptor.open(source, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
                    PdfRenderer(fd).use { it.pageCount }
                }
            }.getOrElse { -1 }
        }
    }
    when (val total = count) {
        null -> Box(Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        0, -1 -> Box(Modifier.fillMaxSize().background(Color.White),
            contentAlignment = Alignment.Center) {
            Text("PDF asli tidak dapat dirender. Pilih mode Teks & sorotan.",
                color = Color.DarkGray)
        }
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize().background(Color(0xFFE7E7E7)),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(total, key = { it }) { index ->
                PdfPageImage(source, index)
            }
        }
    }
}

@Composable
private fun PdfPageImage(source: File, index: Int) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val pixelWidth = with(density) { (configuration.screenWidthDp.dp.toPx() * 1.6f)
        .toInt().coerceIn(720, 1600) }
    val result by produceState<Pair<Bitmap?, Boolean>>(null to false,
        source.path, index, pixelWidth) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                ParcelFileDescriptor.open(source, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                    PdfRenderer(descriptor).use { renderer ->
                        renderer.openPage(index).use { page ->
                            val width = pixelWidth
                            val height = (width.toFloat() / page.width * page.height)
                                .toInt().coerceIn(1, 3500)
                            val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            try {
                                image.eraseColor(android.graphics.Color.WHITE)
                                page.render(image, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                image
                            } catch (error: Exception) {
                                image.recycle()
                                throw error
                            }
                        }
                    }
                }
            }.getOrNull() to true
        }
    }
    val bitmap = result.first
    DisposableEffect(bitmap) {
        val image = bitmap
        onDispose { image?.takeUnless { it.isRecycled }?.recycle() }
    }
    Surface(color = Color.White, modifier = Modifier.fillMaxWidth(), shadowElevation = 2.dp) {
        val shown = bitmap
        if (shown != null && !shown.isRecycled) {
            Image(shown.asImageBitmap(), "Halaman PDF ${index + 1}",
                modifier = Modifier.fillMaxWidth(), contentScale = ContentScale.FillWidth)
        } else Box(Modifier.fillMaxWidth().height(450.dp), contentAlignment = Alignment.Center) {
            Text(if (result.second) "Halaman ${index + 1} gagal dirender."
                else "Memuat halaman ${index + 1}", fontSize = 14.sp, color = Color.Gray)
        }
    }
}
