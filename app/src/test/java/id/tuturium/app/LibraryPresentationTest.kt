package id.tuturium.app

import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryPresentationTest {
    @Test fun remainingMinutesReflectWordsProgressAndSpeed() {
        assertEquals(10, ReadingPosition(0f, 1500).remainingMinutes(INITIAL_MILLIS_PER_WORD, 1f))
        assertEquals(5, ReadingPosition(0.5f, 750).remainingMinutes(INITIAL_MILLIS_PER_WORD, 1f))
        assertEquals(3, ReadingPosition(0.5f, 750).remainingMinutes(INITIAL_MILLIS_PER_WORD, 2f))
        assertEquals(0, ReadingPosition(1f, 0).remainingMinutes(INITIAL_MILLIS_PER_WORD, 1f))
    }
    @Test fun wordCountSkipsWhitespaceAndPunctuation() {
        assertEquals(6, countDocumentWords("Halo, dunia!\nIni adalah sebuah tes."))
    }
    @Test fun sortingByAddedDateDiffersFromLatestRead() {
        val a = Book("a", "Zebra", "TXT", 5, 0, 300, 20, 100)
        val b = Book("b", "Alpha", "TXT", 5, 0, 200, 20, 180)
        assertEquals(listOf("a", "b"), sortBooks(listOf(b, a), "Terbaru").map { it.id })
        assertEquals(listOf("b", "a"), sortBooks(listOf(b, a), "Tanggal ditambahkan").map { it.id })
        assertEquals(listOf("b", "a"), sortBooks(listOf(a, b), "Abjad").map { it.id })
    }
}
