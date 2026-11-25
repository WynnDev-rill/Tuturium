package id.tuturium.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContinuousReadingTest {
    @Test fun fastPlaybackKeepsMoreRealAudioAndAllowsManyShortPhrases() {
        val buffer = SupertonicBuffer()
        assertFalse(buffer.atCapacity(4f, 60_000, 12))
        assertFalse(buffer.atCapacity(4f, 192_000, 56))
        assertTrue(buffer.atCapacity(4f, 360_000, 110))
        assertTrue(buffer.atCapacity(4f, 10_000, 384))
        assertTrue(buffer.atCapacity(1f, 180_000, 28))
        buffer.generated(5_000, 2_000, 0)
        buffer.generated(5_000, 1, 1, cacheHit = true)
        assertFalse(buffer.atCapacity(4f, 720_000, 100))
        assertTrue(buffer.atCapacity(4f, 750_000, 180))
        assertTrue(buffer.atCapacity(4f, 360_000, 100, thermal = 2))
        assertTrue(buffer.atCapacity(4f, 180_000, 40, thermal = 3))
    }

    @Test fun slowerSynthesisBuildsAnInitialReserveButDoesNotWaitIndefinitely() {
        val buffer = SupertonicBuffer()
        buffer.generated(2_000, 2_500, 10_000)
        assertFalse(buffer.ready(4f, 8_000, 4, false, 12_000))
        assertFalse(buffer.ready(4f, 8_000, 4, false, 18_000))
        assertTrue(buffer.ready(4f, 48_000, 4, false, 18_000))
        assertTrue(buffer.ready(1f, 2_000, 1, false, 18_000))
        assertTrue(buffer.ready(4f, 2_000, 1, true, 10_000))
    }

    @Test fun recoveryWaitsForAReserveButStillPlaysTheLastPass() {
        val buffer = SupertonicBuffer()
        assertFalse(buffer.recoveryReady(4f, 4_000L, 1, false))
        assertTrue(buffer.recoveryReady(4f, 32_000L, 4, false))
        assertTrue(buffer.recoveryReady(4f, 1_000L, 1, true))
        assertFalse(buffer.recoveryReady(4f, 0L, 0, true))
    }

    @Test fun inferenceTradesHeadroomForQualityWithoutOscillatingAtTheBoundary() {
        val buffer = SupertonicBuffer()
        assertEquals(5, buffer.inferenceSteps(4f, 0, filling = true, thermal = 0))
        buffer.generated(5_000, 700, 0, steps = 5)
        assertEquals(5, buffer.inferenceSteps(4f, 48_000, filling = false, thermal = 0))
        assertEquals(6, buffer.inferenceSteps(4f, 80_000, filling = false, thermal = 0))
        assertEquals(8, buffer.inferenceSteps(2f, 90_000, filling = false, thermal = 0))
        assertEquals(6, buffer.inferenceSteps(2f, 90_000, filling = false, thermal = 2))
        assertEquals(5, buffer.inferenceSteps(1f, 90_000, filling = false, thermal = 3))
        assertEquals(1, synthesisThreadCount(1))
        assertEquals(2, synthesisThreadCount(3))
        assertEquals(3, synthesisThreadCount(8))
    }

    @Test fun acceleratedSynthesisPrefersSixStepsButRespondsToSlowOrHotDevices() {
        val buffer = SupertonicBuffer(accelerated = true)
        assertEquals(6, buffer.inferenceSteps(4f, 0, filling = true, thermal = 0))
        buffer.generated(5_000, 900, 1, steps = 6)
        assertEquals(6, buffer.inferenceSteps(4f, 15_000, filling = false, thermal = 0))
        assertEquals(8, buffer.inferenceSteps(2f, 90_000, filling = false, thermal = 0))
        repeat(4) { buffer.generated(5_000, 1_600, 2, steps = 6) }
        assertEquals(5, buffer.inferenceSteps(4f, 12_000, filling = false, thermal = 0))
        assertEquals(5, buffer.inferenceSteps(2f, 90_000, filling = false, thermal = 3))
    }

    @Test fun originalPageNumbersSurviveBlankPagesAndOldImports() {
        val original = ReadingDocument(listOf(
            Section("Halaman 1", "Pembukaan."),
            Section("Halaman 2", ""),
            Section("Halaman 3", "Bagian pertama. Bagian kedua.")
        ))
        val olderImport = ReadingDocument(original.sections.filter { it.text.isNotBlank() })
        for (document in listOf(original, olderImport)) {
            assertEquals(mapOf(0 to 0, 2 to 1), document.firstPassByPdfPage())
            assertEquals(2, document.pdfPageForPass(2))
        }
    }

    @Test fun driveShowsTheActualStateForSignedInAndGuestUsers() {
        val guest = TuturiumCloudState()
        assertEquals("Belum terhubung", guest.driveStatus)
        val google = guest.copy(email = "reader@example.invalid")
        assertEquals("Menghubungkan…", google.copy(busy = true).driveStatus)
        assertEquals("Terhubung", google.copy(connected = true).driveStatus)
        assertEquals("Menyinkronkan…", google.copy(connected = true, busy = true).driveStatus)
        assertEquals("Gagal", google.copy(failed = true).driveStatus)
    }
    @Test fun searchCountsOccurrencesRatherThanSentences() {
        assertEquals(listOf(
            ReaderMatch(0, 0..5), ReaderMatch(0, 12..17), ReaderMatch(2, 4..9)
        ), findReaderMatches(listOf("Ketika tiba ketika", "", "dan ketika pulang"), " ketika "))
    }
}
