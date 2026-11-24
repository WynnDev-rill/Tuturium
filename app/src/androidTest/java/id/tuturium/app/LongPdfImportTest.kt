package id.tuturium.app

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class LongPdfImportTest {
    @Test fun importsPdfBeyondPreviousPageLimit() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File.createTempFile("long-document-", ".pdf", directory)
        val library = Library.get(context)
        var imported: Book? = null
        try {
            val pdf = PdfDocument()
            try {
                val paint = Paint().apply { textSize = 16f }
                repeat(1501) { index ->
                    val page = pdf.startPage(PdfDocument.PageInfo.Builder(300, 100, index + 1).create())
                    page.canvas.drawText("Halaman ${index + 1} berisi teks.", 10f, 30f, paint)
                    pdf.finishPage(page)
                }
                file.outputStream().use(pdf::writeTo)
            } finally { pdf.close() }
            val uri = FileProvider.getUriForFile(context, context.packageName + ".updates", file)
            val book = DocumentImporter(context).import(uri) {}
            imported = book
            val result = library.document(book.id)
            assertEquals(1501, result.sections.size)
            assertTrue(result.sections.first().text.contains("Halaman 1"))
            assertTrue(result.sections.last().text.contains("1501"))
        } finally {
            imported?.let { library.delete(it.id); library.deletePermanently(it.id) }
            file.delete()
        }
        Unit
    }
}
