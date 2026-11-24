package id.tuturium.app

import org.junit.Assert.*
import org.junit.Test

class MediaReadingCaptionTest {
    @Test fun advancesWithActualSourceOffsetRatherThanClock() {
        val text = "Satu dua tiga empat lima enam tujuh delapan sembilan sepuluh."
        val caption = MediaReadingCaption(PreparedSpeech(text, IntArray(text.length) { it }))
        assertEquals("Satu dua tiga empat lima enam", caption.at(0))
        assertEquals("Satu dua tiga empat lima enam", caption.at(text.indexOf("lima")))
        assertEquals("tujuh delapan sembilan sepuluh.", caption.at(text.indexOf("tujuh")))
    }

    @Test fun mapsFilteredTextToOriginalPosition() {
        val text = "Satu dua tiga empat lima enam tujuh."
        val offsets = IntArray(text.length) { if (it < 10) it else it + 20 }
        val caption = MediaReadingCaption(PreparedSpeech(text, offsets))
        assertEquals("tujuh.", caption.at(offsets[text.indexOf("tujuh")]))
    }

    @Test fun boundsLongTextAndHandlesEmptyInput() {
        val text = "a".repeat(140)
        assertTrue(MediaReadingCaption(PreparedSpeech(text, IntArray(text.length) { it })).at(0).length <= 90)
        assertEquals("", MediaReadingCaption(PreparedSpeech("", IntArray(0))).at(0))
    }
}
