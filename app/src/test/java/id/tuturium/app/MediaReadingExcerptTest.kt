package id.tuturium.app

import org.junit.Assert.*
import org.junit.Test

class MediaReadingExcerptTest {
    @Test fun excerptMovesWithSpeechOffsetWithoutSplittingWords() {
        val text = "Pertama kita membaca buku ini. Berikutnya kita memahami bagian lain."
        assertEquals("Pertama kita membaca buku", mediaReadingExcerpt(text, 0, 26))
        assertEquals("Berikutnya kita memahami", mediaReadingExcerpt(text, text.indexOf("memahami"), 26))
    }

    @Test fun normalizesWhitespaceAndHandlesMissingText() {
        assertEquals("Bacaan hari ini.", mediaReadingExcerpt("Bacaan\n hari\t ini.", 7))
        assertEquals("", mediaReadingExcerpt("", 10))
    }

    @Test fun shortSentenceKeepsItsContext() {
        assertEquals("Aku membaca buku.", mediaReadingExcerpt("Aku membaca buku.", 4))
    }

    @Test fun seekToEndDoesNotReturnEarlierText() {
        val text = "Kalimat yang panjang sekali untuk dibaca."
        assertTrue(mediaReadingExcerpt(text, text.lastIndex, 18).contains("dibaca."))
    }
}
