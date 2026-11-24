package id.tuturium.app

import org.junit.Assert.*
import org.junit.Test

class ReaderLibraryUpdateTest {
    @Test fun durationCanExceedOneThousandMinutesAndSupportsFourTimesSpeed() {
        assertEquals(2000, estimateRemainingMinutes(300000, 100, 0, 1f))
        assertEquals(500, estimateRemainingMinutes(300000, 100, 0, 4f))
    }
    @Test fun highlightUsesOnlyCurrentPassAndIntersectingWords() {
        val first = PdfWordHit("satu", .1f, .1f, .2f, .2f, WordSelection(4, 0))
        val second = PdfWordHit("dua", .3f, .1f, .4f, .2f, WordSelection(4, 5))
        val other = first.copy(selection = WordSelection(5, 0))
        assertEquals(listOf(first, second), pdfHighlightWords(listOf(first, second, other), 4, null))
        assertEquals(listOf(second), pdfHighlightWords(listOf(first, second, other), 4, 5..7))
        assertEquals(emptyList<PdfWordHit>(), pdfHighlightWords(listOf(first, second), 5, 0..2))
    }
}
