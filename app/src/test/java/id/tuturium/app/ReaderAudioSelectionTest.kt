package id.tuturium.app

import org.junit.Assert.*
import org.junit.Test

class ReaderAudioSelectionTest {
    @Test fun findsNearestWordOnSameLineButNotAcrossPage() {
        val hit = PdfWordHit("kata", .10f, .20f, .20f, .23f, WordSelection(1, 0))
        assertEquals(hit, nearestPdfWord(listOf(hit), .207f, .225f))
        assertNull(nearestPdfWord(listOf(hit), .8f, .8f))
    }
    @Test fun boostRampsOnlyAtHigherVolumeAndIsBounded() {
        assertEquals(0, narrationBoostMb(0f))
        assertEquals(0, narrationBoostMb(.5f))
        assertEquals(950, narrationBoostMb(1f))
        assertEquals(950, narrationBoostMb(2f))
        assertTrue(narrationBoostMb(.8f) in 1..949)
    }
}
