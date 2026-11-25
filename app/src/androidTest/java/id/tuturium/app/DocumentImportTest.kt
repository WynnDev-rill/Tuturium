package id.tuturium.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.content.FileProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DocumentImportTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val library = Library.get(context)
    private val folder = File(context.cacheDir, "updates").apply { mkdirs() }

    @Test fun txtPersistsProgressAndBookmarks() = runBlocking {
        checkImport("Pratinjau.txt", { it.writeText("\uFEFFKalimat pertama.\r\nKalimat kedua.") }) { book, doc ->
            assertEquals("TXT", book.format)
            assertEquals(listOf("Kalimat pertama.", "Kalimat kedua."), doc.passes.map { it.text })
            library.position(book.id, 1)
            library.toggleBookmark(book.id, 1, "Kalimat kedua.")
            assertEquals(1, library.book(book.id)?.position)
            assertEquals(listOf(Bookmark(1, "Kalimat kedua.")), library.bookmarks(book.id))
            library.rename(book.id, "Judul baru")
            assertEquals("Judul baru", library.book(book.id)?.title)
        }
    }

    @Test fun docxCombinesRunsAndSkipsDeletedText() = runBlocking {
        checkImport("Surat.docx", { file ->
            zip(file, mapOf("word/document.xml" to """
                <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                  <w:body><w:p><w:r><w:t>Selamat </w:t></w:r><w:r><w:t>pagi.</w:t></w:r>
                  <w:del><w:r><w:delText>Dihapus</w:delText></w:r></w:del></w:p>
                  <w:p><w:r><w:t>Selamat siang.</w:t></w:r></w:p></w:body>
                </w:document>
            """.trimIndent()))
        }) { book, doc ->
            assertEquals("DOCX", book.format)
            assertEquals(listOf("Selamat pagi.", "Selamat siang."), doc.passes.map { it.text })
        }
    }

    @Test fun epubFollowsSpineAndDecodesEscapedEntryPath() = runBlocking {
        checkImport("Buku.epub", { file ->
            zip(file, linkedMapOf(
                "META-INF/container.xml" to "<container><rootfiles><rootfile full-path='OPS/book.opf'/></rootfiles></container>",
                "OPS/book.opf" to """<package xmlns:dc="http://purl.org/dc/elements/1.1/">
                    <metadata><dc:title>Buku Uji</dc:title></metadata><manifest>
                    <item id="b" href="bab-b.xhtml" media-type="application/xhtml+xml"/>
                    <item id="a" href="bab%20a.xhtml" media-type="application/xhtml+xml"/>
                    </manifest><spine><itemref idref="a"/><itemref idref="b"/></spine></package>""",
                "OPS/bab-b.xhtml" to "<html><body><h1>Bab B</h1><p>Kalimat akhir.</p></body></html>",
                "OPS/bab a.xhtml" to "<html><body><h1>Bab A</h1><p>Kalimat awal.</p></body></html>"
            ))
        }) { book, doc ->
            assertEquals("Buku Uji", book.title)
            assertEquals(listOf("Bab A", "Bab B"), doc.sections.map { it.title })
            assertTrue(doc.sections.first().text.contains("Kalimat awal."))
        }
    }

    @Test fun pdfExtractsTextByPage() = runBlocking {
        checkImport("Tulisan.pdf", { file ->
            PDFBoxResourceLoader.init(context)
            PDDocument().use { pdf ->
                listOf("Halaman satu.", "Halaman dua.").forEach { phrase ->
                    val page = PDPage().also(pdf::addPage)
                    PDPageContentStream(pdf, page).use { stream ->
                        stream.beginText()
                        stream.setFont(PDType1Font.HELVETICA, 28f)
                        stream.newLineAtOffset(50f, 700f)
                        stream.showText(phrase)
                        stream.endText()
                    }
                }
                pdf.save(file)
            }
        }) { _, doc ->
            assertEquals(listOf("Halaman 1", "Halaman 2"), doc.sections.map { it.title })
            assertEquals(listOf("Halaman satu.", "Halaman dua."), doc.passes.map { it.text })
        }
    }

    @Test fun layoutFiltersNativePdfWithoutChangingSourcePositions() = runBlocking {
        checkImport("TataLetak.pdf", { file ->
            PDFBoxResourceLoader.init(context)
            PDDocument().use { pdf ->
                for (number in 1..4) {
                    val page = PDPage().also(pdf::addPage)
                    PDPageContentStream(pdf, page).use { stream ->
                        fun line(text: String, x: Float, y: Float, size: Float) {
                            stream.beginText()
                            stream.setFont(PDType1Font.HELVETICA, size)
                            stream.newLineAtOffset(x, y)
                            stream.showText(text)
                            stream.endText()
                        }
                        line("Bab IV", 70f, 765f, 11f)
                        line("Penduduk berjumlah 1.874.161 orang.", 70f, 540f, 15f)
                        line("Catatan pinggir", 70f, 130f, 10f)
                        line(number.toString(), 300f, 26f, 10f)
                    }
                }
                pdf.save(file)
            }
        }) { book, raw ->
            val index = raw.passes.indexOfFirst { "1.874.161" in it.text }
            library.position(book.id, index, 3)
            library.toggleBookmark(book.id, index, "Penduduk")
            val pdf = checkNotNull(library.originalPdf(book.id))
            val originalBytes = pdf.readBytes()
            PdfMarginStore.write(context, book.id, PdfMarginSettings(all = PdfMargins(bottom = .75f)))
            val processed = library.narrationDocument(book.id, detectLayout = true)
            val pipeline = NarrationPipeline(processed, ListeningOptions(skipEnabled = true))
            val all = processed.passes.indices.joinToString(" ") { pipeline.prepare(it).text }
            assertEquals(1, Regex("Bab empat").findAll(all).count())
            assertEquals(4, Regex("satu juta").findAll(all).count())
            assertFalse(all.contains("Catatan pinggir"))
            assertEquals(raw.passes, processed.passes)
            assertEquals(index, library.book(book.id)?.position)
            assertEquals(3, library.book(book.id)?.wordOffset)
            assertEquals(index, library.bookmarks(book.id).single().index)
            assertArrayEquals(originalBytes, pdf.readBytes())
        }
    }

    @Test fun pdfImportsAesEncryptedTextWhenTheFileAllowsOpeningWithoutAPassword() = runBlocking {
        checkImport("Enkripsi.pdf", { file ->
            PDFBoxResourceLoader.init(context)
            PDDocument().use { pdf ->
                val page = PDPage().also(pdf::addPage)
                PDPageContentStream(pdf, page).use { stream ->
                    stream.beginText()
                    stream.setFont(PDType1Font.HELVETICA, 28f)
                    stream.newLineAtOffset(50f, 700f)
                    stream.showText("PDF terenkripsi.")
                    stream.endText()
                }
                pdf.protect(StandardProtectionPolicy("owner-test", "", AccessPermission()).apply {
                    encryptionKeyLength = 128
                    setPreferAES(true)
                })
                pdf.save(file)
            }
        }) { _, document ->
            assertEquals(listOf("PDF terenkripsi."), document.passes.map { it.text })
        }
    }

    @Test fun pdfRecognizesScannedLatinTextWithBundledModel() = runBlocking {
        checkImport("Pindai.pdf", { file ->
            PDFBoxResourceLoader.init(context)
            PDDocument().use { pdf ->
                val page = PDPage().also(pdf::addPage)
                val bitmap = Bitmap.createBitmap(900, 200, Bitmap.Config.ARGB_8888)
                try {
                    Canvas(bitmap).apply {
                        drawColor(Color.WHITE)
                        drawText("SELAMAT PAGI", 30f, 115f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.BLACK
                            textSize = 70f
                        })
                    }
                    PDPageContentStream(pdf, page).use { stream ->
                        stream.drawImage(LosslessFactory.createFromImage(pdf, bitmap), 50f, 600f, 500f, 110f)
                    }
                    pdf.save(file)
                } finally {
                    bitmap.recycle()
                }
            }
        }) { book, document ->
            assertTrue(document.sections.single().text.contains("SELAMAT PAGI", ignoreCase = true))
            PdfMarginStore.write(context, book.id, PdfMarginSettings(all = PdfMargins(top = .4f)))
            val processed = library.narrationDocument(book.id, detectLayout = false)
            assertEquals(document.passes, processed.passes)
            assertTrue(NarrationPipeline(processed, ListeningOptions()).prepare(0).text.isBlank())
        }
    }

    private suspend fun checkImport(name: String, fill: (File) -> Unit, verify: suspend (Book, ReadingDocument) -> Unit) {
        val file = File(folder, name)
        try {
            fill(file)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.updates", file)
            val book = DocumentImporter(context).import(uri) {}
            try {
                verify(book, library.document(book.id))
            } finally {
                library.delete(book.id)
                assertNull(library.book(book.id))
                assertTrue(library.trashed().any { it.id == book.id })
                library.restore(book.id)
                assertNotNull(library.book(book.id))
                library.delete(book.id)
                library.deletePermanently(book.id)
            }
            assertNull(library.book(book.id))
            assertTrue(library.bookmarks(book.id).isEmpty())
        } finally {
            file.delete()
        }
    }

    private fun zip(file: File, entries: Map<String, String>) {
        ZipOutputStream(file.outputStream()).use { zip ->
            entries.forEach { (path, text) ->
                zip.putNextEntry(ZipEntry(path))
                zip.write(text.toByteArray())
                zip.closeEntry()
            }
        }
    }
}
