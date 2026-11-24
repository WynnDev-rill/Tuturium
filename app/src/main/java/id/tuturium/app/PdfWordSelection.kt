package id.tuturium.app

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.io.MemoryUsageSetting
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.text.TextPosition
import android.content.Context
import java.io.File
import java.util.Locale

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
    val right: Float, val bottom: Float, val selection: WordSelection? = null)

internal fun readPdfWordHits(context: Context, source: File, pageIndex: Int,
    passes: List<IndexedValue<Pass>>): List<PdfWordHit> {
    PDFBoxResourceLoader.init(context)
    val hits = mutableListOf<PdfWordHit>()
    PDDocument.load(source, MemoryUsageSetting.setupTempFileOnly()).use { document ->
        require(document.currentAccessPermission.canExtractContent())
        if (pageIndex !in 0 until document.numberOfPages) return emptyList()
        val stripper = object : PDFTextStripper() {
            override fun writeString(text: String, textPositions: MutableList<TextPosition>) {
                if (textPositions.isEmpty() || text.isBlank()) return
                // PDFBox emits individual words with their associated glyphs.
                val tokens = pdfWordPattern.findAll(text).toList()
                if (tokens.size != 1) return
                val first = textPositions.first()
                val width = first.pageWidth.coerceAtLeast(1f)
                val height = first.pageHeight.coerceAtLeast(1f)
                val left = textPositions.minOf { it.x / width }
                val right = textPositions.maxOf { (it.x + it.width) / width }
                val top = textPositions.minOf { (it.y - it.height) / height }
                val bottom = textPositions.maxOf { it.y / height }
                hits.add(PdfWordHit(tokens.single().value, left, top, right, bottom))
            }
        }.apply {
            sortByPosition = true
            startPage = pageIndex + 1
            endPage = pageIndex + 1
        }
        stripper.getText(document)
    }
    val selections = matchPdfWords(hits.map { it.text }, passes)
    return hits.mapIndexed { i, hit -> hit.copy(selection = selections[i]) }
}
