package id.tuturium.app

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.io.MemoryUsageSetting
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.text.TextPosition
import android.content.Context
import java.io.File
import java.util.Locale
import kotlinx.coroutines.tasks.await

private val pdfWordPattern = Regex("[\\p{L}\\p{N}\\p{M}]+")
private fun pdfKey(word: String) = java.text.Normalizer.normalize(word, java.text.Normalizer.Form.NFKC)
    .lowercase(Locale.ROOT)

/** Align the PDF text layer to the stored speech passages, retaining exact offsets.
 * Unmatched glyphs (e.g. different OCR) are not assigned a fabricated position. */
internal fun matchPdfWords(words: List<String>, passes: List<IndexedValue<Pass>>): List<WordSelection?> {
    val tokens = passes.flatMap { (index, pass) ->
        pdfWordPattern.findAll(pass.text).map { pdfKey(it.value) to WordSelection(index, it.range.first) }.toList()
    }
    var cursor = 0
    return words.map { word ->
        val key = pdfKey(word)
        val match = (cursor until minOf(tokens.size, cursor + 12)).firstOrNull { tokens[it].first == key }
        if (match == null) null else tokens[match].second.also { cursor = match + 1 }
    }
}

internal data class PdfWordHit(val text: String, val left: Float, val top: Float,
    val right: Float, val bottom: Float, val selection: WordSelection? = null, val fontScale: Float? = null)

private class PdfWordStripper(
    first: Int, last: Int, private val pageReady: (Int, List<PdfWordHit>) -> Unit
) : PDFTextStripper() {
    private val words = mutableListOf<PdfWordHit>()

    init {
        sortByPosition = true
        startPage = first + 1
        endPage = last + 1
    }

    override fun writeString(text: String, textPositions: MutableList<TextPosition>) {
        if (textPositions.isEmpty() || text.isBlank()) return
        val tokens = pdfWordPattern.findAll(text).toList()
        val first = textPositions.first()
        val width = first.pageWidth.coerceAtLeast(1f)
        val height = first.pageHeight.coerceAtLeast(1f)
        val glyphs = if (tokens.size == 1) emptyList() else textPositions.flatMap { glyph ->
            List(glyph.unicode?.length?.coerceAtLeast(1) ?: 1) { glyph }
        }
        for (token in tokens) {
            val positions = if (tokens.size == 1) textPositions else
                glyphs.drop(token.range.first).take(token.value.length)
            if (positions.isEmpty()) continue
            words.add(PdfWordHit(token.value,
                positions.minOf { it.x / width },
                positions.minOf { (it.y - it.height) / height },
                positions.maxOf { (it.x + it.width) / width },
                positions.maxOf { it.y / height },
                fontScale = positions.maxOf { it.fontSizeInPt } / height))
        }
    }

    override fun endPage(page: com.tom_roush.pdfbox.pdmodel.PDPage) {
        super.endPage(page)
        pageReady(currentPageNo - 1, words.toList())
        words.clear()
    }
}

internal fun readPdfWords(context: Context, source: File, first: Int = 0, last: Int = Int.MAX_VALUE - 1,
    pageReady: (Int, List<PdfWordHit>) -> Unit) {
    PDFBoxResourceLoader.init(context)
    PDDocument.load(source, MemoryUsageSetting.setupTempFileOnly()).use { document ->
        require(document.currentAccessPermission.canExtractContent())
        if (first !in 0 until document.numberOfPages) return
        val sink = object : java.io.Writer() {
            override fun write(cbuf: CharArray, off: Int, len: Int) = Unit
            override fun flush() = Unit
            override fun close() = Unit
        }
        PdfWordStripper(first, minOf(last, document.numberOfPages - 1), pageReady).writeText(document, sink)
    }
}

internal fun readPdfWordHits(context: Context, source: File, pageIndex: Int,
    passes: List<IndexedValue<Pass>>): List<PdfWordHit> {
    var hits = emptyList<PdfWordHit>()
    readPdfWords(context, source, pageIndex, pageIndex) { _, words -> hits = words }
    return alignPdfHits(hits, passes)
}

internal fun alignPdfHits(hits: List<PdfWordHit>, passes: List<IndexedValue<Pass>>): List<PdfWordHit> {
    val selections = matchPdfWords(hits.map { it.text }, passes)
    return hits.mapIndexed { i, hit -> hit.copy(selection = selections[i]) }
}

/** Allow a small finger-touch margin around actual glyphs, never a distant word. */
internal fun nearestPdfWord(hits: List<PdfWordHit>, x: Float, y: Float): PdfWordHit? =
    hits.filter { x in (it.left - .014f)..(it.right + .014f) &&
        y in (it.top - .012f)..(it.bottom + .012f) }
        .minByOrNull {
            val dx = maxOf(it.left - x, x - it.right, 0f)
            val dy = maxOf(it.top - y, y - it.bottom, 0f)
            dx * dx + 4f * dy * dy + .001f * kotlin.math.abs(x - (it.left + it.right) / 2f)
        }

/** OCR runs only for pages whose native or PDFBox text layer cannot resolve a touch.
 * Coordinates are from the actual rendered bitmap, so scanned and unusual PDFs work. */
internal suspend fun ocrPdfWordHits(bitmap: android.graphics.Bitmap,
    passes: List<IndexedValue<Pass>>): List<PdfWordHit> {
    val recognizer = com.google.mlkit.vision.text.TextRecognition.getClient(
        com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS)
    try {
        val result = recognizer.process(com.google.mlkit.vision.common.InputImage.fromBitmap(bitmap, 0)).await()
        val hits = result.textBlocks.flatMap { it.lines }.flatMap { it.elements }.flatMap { element ->
            val rect = element.boundingBox ?: return@flatMap emptyList()
            pdfWordPattern.findAll(element.text).map { token ->
                val left = rect.left + rect.width() * (token.range.first.toFloat() / element.text.length)
                val right = rect.left + rect.width() * ((token.range.last + 1f) / element.text.length)
                PdfWordHit(token.value, left / bitmap.width, rect.top.toFloat() / bitmap.height,
                    right / bitmap.width, rect.bottom.toFloat() / bitmap.height)
            }.toList()
        }
        return alignPdfHits(hits, passes)
    } finally { recognizer.close() }
}

/** Select only words mapped to the active speech pass and overlapping its range. */
internal fun pdfHighlightWords(hits: List<PdfWordHit>, passIndex: Int, range: IntRange?): List<PdfWordHit> =
    hits.filter { hit ->
        val selection = hit.selection
        selection != null && selection.passIndex == passIndex &&
            (range == null || (selection.offset <= range.last &&
                selection.offset + hit.text.length - 1 >= range.first))
    }
