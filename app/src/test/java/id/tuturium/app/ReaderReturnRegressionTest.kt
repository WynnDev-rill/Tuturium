package id.tuturium.app

import org.junit.Assert.*
import org.junit.Test

class ReaderReturnRegressionTest {
    @Test fun readerTimeUsesHoursForLongBooks() {
        assertEquals("04:05:18", formatPlaybackTime(14718))
        assertEquals("03:18", formatPlaybackTime(198))
        assertEquals("01:00:00", formatPlaybackTime(3600))
    }
    @Test fun centeringUsesDeadZoneAndRespectsManualBrowsing() {
        assertEquals(0f, readerCenterDelta(480f, 0, 1000, 40f, false), 0f)
        assertEquals(200f, readerCenterDelta(700f, 0, 1000, 40f, false), 0f)
        assertEquals(0f, readerCenterDelta(1100f, 0, 1000, 40f, true), 0f)
        assertEquals(0f, readerCenterDelta(700f, 0, 1000, 40f, true), 0f)
    }
    @Test fun filtersOnlyIncludeMatchingDocuments() {
        val pdf = Book("a", "Buku", "PDF", 10, 0, 0)
        val web = pdf.copy(format = "WEB", position = 3)
        assertTrue(matchesLibraryFilter(pdf, "Belum dibaca", false))
        assertFalse(matchesLibraryFilter(web, "Belum dibaca", false))
        assertTrue(matchesLibraryFilter(web, "Baca nanti", true))
        assertFalse(matchesLibraryFilter(pdf, "Baca nanti", false))
        assertTrue(matchesLibraryFilter(pdf, "PDF", false))
        assertFalse(matchesLibraryFilter(web, "PDF", false))
        assertTrue(matchesLibraryFilter(web, "Halaman web", false))
    }
}
