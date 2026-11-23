package id.tuturium.app

import android.graphics.pdf.PdfDocument
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class PdfSourceStorageTest {
    @Test fun pdfKeepsOriginalPageLayoutAndCleanupOnPermanentDelete() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val library = Library.get(context)
        val document = PdfDocument()
        val info = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(info)
        page.canvas.drawColor(android.graphics.Color.WHITE)
        val paint = android.graphics.Paint().apply {
            textSize = 22f; color = android.graphics.Color.BLACK
        }
        page.canvas.drawText("Judul Bab Asli", 100f, 160f, paint)
        document.finishPage(page)
        val source = File(context.cacheDir, "layout-test.pdf")
        document.writeTo(source.outputStream())
        document.close()
        val book = library.add("PDF layout test", "PDF",
            ReadingDocument(listOf(Section("Halaman 1", "Judul Bab Asli."))))
        try {
            library.saveOriginalPdf(book.id, source)
            val persisted = requireNotNull(library.originalPdf(book.id))
            assertTrue(persisted.isFile)
            val fd = android.os.ParcelFileDescriptor.open(persisted,
                android.os.ParcelFileDescriptor.MODE_READ_ONLY)
            android.graphics.pdf.PdfRenderer(fd).use { renderer ->
                assertEquals(1, renderer.pageCount)
                renderer.openPage(0).use { shown ->
                    assertEquals(595, shown.width)
                    assertEquals(842, shown.height)
                }
            }
            library.delete(book.id)
            library.deletePermanently(book.id)
            assertNull(library.originalPdf(book.id))
        } finally {
            source.delete()
            library.delete(book.id)
            library.deletePermanently(book.id)
        }
    }
}
