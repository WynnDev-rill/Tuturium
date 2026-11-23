package id.tuturium.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Xml
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File
import java.io.InputStream
import java.net.URI
import java.util.Locale
import java.util.zip.ZipFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import org.xmlpull.v1.XmlPullParser

class DocumentImporter(private val context: Context) {
    suspend fun import(uri: Uri, onProgress: (String) -> Unit): Book = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val name = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { if (it.moveToFirst()) it.getString(0) else null } ?: "Dokumen"
        val format = name.substringAfterLast('.', "").lowercase(Locale.ROOT).takeIf { it in formats }
            ?: mimeFormats[resolver.getType(uri)]
            ?: error("Pilih dokumen PDF, EPUB, DOCX, atau TXT.")
        val file = File.createTempFile("import-", ".$format", context.cacheDir)
        try {
            resolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    val buffer = ByteArray(65536)
                    var total = 0L
                    while (true) {
                        ensureActive()
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        require(total <= 100L * 1024 * 1024) { "Ukuran dokumen melebihi 100 MB." }
                        output.write(buffer, 0, read)
                    }
                }
            } ?: error("Berkas tidak dapat dibuka.")
            val defaultTitle = name.substringBeforeLast('.').ifBlank { "Dokumen" }
            val (title, sections) = when (format) {
                "pdf" -> defaultTitle to pdf(file, onProgress)
                "epub" -> ZipFile(file).use(::epub).let { (title, sections) ->
                    title.ifBlank { defaultTitle } to sections
                }
                "docx" -> defaultTitle to ZipFile(file).use { zip ->
                    listOf(Section(defaultTitle, docx(zip)))
                }
                else -> defaultTitle to listOf(Section(defaultTitle, plainText(file)))
            }
            val readable = sections.map { it.copy(text = it.text.trim()) }.filter { it.text.isNotBlank() }
            require(readable.sumOf { it.text.length.toLong() } <= MAX_TEXT) { "Teks dokumen terlalu panjang." }
            ensureActive()
            Library.get(context).add(title, format.uppercase(Locale.ROOT), ReadingDocument(readable))
        } finally {
            file.delete()
        }
    }

    private fun plainText(file: File): String {
        val data = file.inputStream().use { it.limited(MAX_TEXT * 4) }
        val charset = when {
            data.startsWithBytes(0xff, 0xfe) -> Charsets.UTF_16LE
            data.startsWithBytes(0xfe, 0xff) -> Charsets.UTF_16BE
            else -> Charsets.UTF_8
        }
        val text = runCatching { charset.newDecoder().decode(java.nio.ByteBuffer.wrap(data)).toString() }
            .getOrElse { data.toString(charset("windows-1252")) }
        require('\u0000' !in text) { "Berkas TXT berisi data biner." }
        return text.removePrefix("\uFEFF").replace("\r\n", "\n").replace('\r', '\n')
    }

    private suspend fun pdf(file: File, progress: (String) -> Unit): List<Section> {
        PDFBoxResourceLoader.init(context)
        val extracted = PDDocument.load(file, com.tom_roush.pdfbox.io.MemoryUsageSetting.setupTempFileOnly())
            .use { pdf ->
                require(pdf.numberOfPages <= 1500) { "PDF melebihi 1.500 halaman." }
                require(pdf.currentAccessPermission.canExtractContent()) { "PDF membatasi ekstraksi teks." }
                val stripper = PDFTextStripper().apply { sortByPosition = true }
                var total = 0L
                List(pdf.numberOfPages) { page ->
                    kotlin.coroutines.coroutineContext.ensureActive()
                    progress("Membaca halaman ${page + 1} dari ${pdf.numberOfPages}")
                    stripper.startPage = page + 1
                    stripper.endPage = page + 1
                    normalizePdf(stripper.getText(pdf)).also {
                        total += it.length
                        require(total <= MAX_TEXT) { "Teks PDF terlalu panjang." }
                    }
                }
            }
        if (extracted.all { it.any(Char::isLetterOrDigit) }) {
            return extracted.mapIndexed { index, text -> Section("Halaman ${index + 1}", text) }
        }
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        try {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    var total = 0L
                    return extracted.mapIndexed { index, text ->
                        kotlin.coroutines.coroutineContext.ensureActive()
                        val detected = if (text.any(Char::isLetterOrDigit)) text else {
                            progress("Memindai halaman ${index + 1} dari ${extracted.size}")
                            renderer.openPage(index).use { page ->
                                val scale = 1600f / maxOf(page.width, page.height)
                                val bitmap = Bitmap.createBitmap(
                                    (page.width * scale).toInt().coerceAtLeast(1),
                                    (page.height * scale).toInt().coerceAtLeast(1), Bitmap.Config.ARGB_8888
                                )
                                var submitted = false
                                try {
                                    bitmap.eraseColor(android.graphics.Color.WHITE)
                                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                    val task = recognizer.process(InputImage.fromBitmap(bitmap, 0))
                                    task.addOnCompleteListener { bitmap.recycle() }
                                    submitted = true
                                    task.await().textBlocks.joinToString("\n") { block ->
                                        block.lines.joinToString(" ") { it.text }
                                    }
                                } finally {
                                    if (!submitted) bitmap.recycle()
                                }
                            }
                        }
                        total += detected.length
                        require(total <= MAX_TEXT) { "Teks PDF terlalu panjang." }
                        Section("Halaman ${index + 1}", detected)
                    }
                }
            }
        } finally {
            recognizer.close()
        }
    }

    private fun normalizePdf(text: String): String = text.trim().replace("\r\n", "\n")
        .split(Regex("\\n[ \\t]*\\n+")).joinToString("\n") {
            it.replace(Regex("(?<=\\p{L})-\\n[ \\t]*(?=\\p{Ll})"), "")
                .replace(Regex("[ \\t]*\\n[ \\t]*"), " ")
        }

    private fun epub(zip: ZipFile): Pair<String, List<Section>> {
        val container = zip.xml("META-INF/container.xml")
        val root = container.getElementsByTag("rootfile").first()?.attr("full-path")
            ?.takeIf(String::isNotBlank) ?: error("Daftar isi EPUB tidak ditemukan.")
        val opf = zip.xml(root)
        val title = opf.getAllElements().firstOrNull { it.tagName().substringAfter(':') == "title" }
            ?.text().orEmpty()
        val items = opf.getElementsByTag("item").associateBy { it.attr("id") }
        val spine = opf.getElementsByTag("itemref")
        require(spine.size <= 1500) { "EPUB melebihi 1.500 bagian." }
        var total = 0L
        val sections = spine.mapIndexedNotNull { index, ref ->
            if (ref.attr("linear") == "no") return@mapIndexedNotNull null
            val entry = items[ref.attr("idref")] ?: error("Bagian EPUB tidak lengkap.")
            if (entry.attr("media-type") !in setOf("application/xhtml+xml", "text/html")) {
                return@mapIndexedNotNull null
            }
            val path = URI("file", "", "/$root", null).resolve(entry.attr("href").replace(" ", "%20")).normalize()
            require(path.scheme == "file" && path.host == null)
            val html = zip.stream(path.path.removePrefix("/")).use {
                Jsoup.parse(it.limited(MAX_TEXT).inputStream(), null, "")
            }
            val heading = html.selectFirst("h1,h2,h3")?.text()?.takeIf(String::isNotBlank)
                ?: "Bagian ${index + 1}"
            html.select("script, style, nav, [hidden]").remove()
            html.select("br").append("\\n")
            html.select("p, h1, h2, h3, h4, h5, h6, li, blockquote, dt, dd, tr, pre, div, section, article").append("\\n")
            val content = html.body().text().replace("\\n", "\n")
            total += content.length
            require(total <= MAX_TEXT) { "Teks EPUB terlalu panjang." }
            Section(heading, content)
        }
        return title to sections
    }

    private fun docx(zip: ZipFile): String = zip.stream("word/document.xml").use { input ->
        val parser = Xml.newPullParser().apply {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
            setInput(input.limited(MAX_TEXT * 4).inputStream(), null)
        }
        val result = StringBuilder()
        var skipped = 0
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            require(event != XmlPullParser.DOCDECL) { "Dokumen berisi deklarasi XML yang tidak didukung." }
            if (parser.namespace in wordNamespaces) {
                if (event == XmlPullParser.START_TAG) when (parser.name) {
                    "del", "moveFrom" -> skipped++
                    "t" -> if (skipped == 0) result.append(parser.nextText())
                    "tab" -> if (skipped == 0) result.append('\t')
                    "br", "cr" -> if (skipped == 0) result.append('\n')
                }
                if (event == XmlPullParser.END_TAG) when (parser.name) {
                    "del", "moveFrom" -> skipped = (skipped - 1).coerceAtLeast(0)
                    "p" -> if (skipped == 0) result.append('\n')
                }
            }
            require(result.length <= MAX_TEXT) { "Teks DOCX terlalu panjang." }
            event = parser.nextToken()
        }
        result.toString()
    }

    private fun ZipFile.xml(name: String): Document = stream(name).use {
        Jsoup.parse(it.limited(MAX_TEXT).inputStream(), null, "", Parser.xmlParser())
    }

    private fun ZipFile.stream(name: String): InputStream = getEntry(name)?.let(::getInputStream)
        ?: error("Struktur dokumen tidak lengkap atau dilindungi DRM.")

    private fun ByteArray.startsWithBytes(first: Int, second: Int) =
        size >= 2 && this[0].toInt() and 255 == first && this[1].toInt() and 255 == second

    private fun InputStream.limited(limit: Int): ByteArray {
        val result = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(16384)
        while (true) {
            val read = read(buffer)
            if (read < 0) return result.toByteArray()
            require(result.size() + read <= limit) { "Bagian dokumen terlalu besar." }
            result.write(buffer, 0, read)
        }
    }

    companion object {
        const val MAX_TEXT = 2_000_000
        val formats = setOf("pdf", "epub", "docx", "txt")
        val mimeFormats = mapOf("application/pdf" to "pdf", "application/epub+zip" to "epub",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" to "docx",
            "text/plain" to "txt")
        val wordNamespaces = setOf("http://schemas.openxmlformats.org/wordprocessingml/2006/main",
            "http://purl.oclc.org/ooxml/wordprocessingml/main")
    }
}
