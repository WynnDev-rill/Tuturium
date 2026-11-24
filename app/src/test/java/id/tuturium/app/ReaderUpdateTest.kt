package id.tuturium.app

import org.junit.Assert.*
import org.junit.Test

class ReaderUpdateTest {
    @Test fun normalizesQuietNarrationMoreStronglyWithoutClipping() {
        val quiet = floatArrayOf(0.03f, -0.06f, 0.04f)
        val gain = narrationGain(quiet)
        assertTrue(gain > 4f)
        assertTrue(amplifyNarration(0.03f, gain) > 0.12f)
        for (sample in listOf(-1f, -0.5f, 0f, 0.5f, 1f)) {
            assertTrue(kotlin.math.abs(amplifyNarration(sample, gain)) < 0.96f)
        }
    }

    @Test fun mapsRepeatedPdfWordsInReadingOrder() {
        val passes = listOf(IndexedValue(7, Pass("kata satu kata.", 0, 0, 0)),
            IndexedValue(8, Pass("kata dua.", 0, 1, 0)))
        val matched = matchPdfWords(listOf("kata", "satu", "kata", "kata", "dua"), passes)
        assertEquals(listOf(WordSelection(7, 0), WordSelection(7, 5), WordSelection(7, 10),
            WordSelection(8, 0), WordSelection(8, 5)), matched)
    }

    @Test fun unmatchedPdfWordIsNotSilentlyMappedToAnotherWord() {
        val passes = listOf(IndexedValue(0, Pass("satu dua", 0, 0, 0)))
        assertEquals(listOf(null, WordSelection(0, 0), WordSelection(0, 5)),
            matchPdfWords(listOf("asing", "satu", "dua"), passes))
    }
}
