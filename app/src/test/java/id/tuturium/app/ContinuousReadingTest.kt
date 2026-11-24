package id.tuturium.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContinuousReadingTest {
    @Test fun fastPlaybackKeepsMoreRealAudioAndAllowsManyShortPhrases() {
        val buffer = SupertonicBuffer()
        assertFalse(buffer.atCapacity(4f, 60_000, 12))
        assertTrue(buffer.atCapacity(4f, 192_000, 28))
        assertTrue(buffer.atCapacity(4f, 10_000, 56))
        assertTrue(buffer.atCapacity(1f, 90_000, 20))
    }

    @Test fun slowerSynthesisBuildsAnInitialReserveButDoesNotWaitIndefinitely() {
        val buffer = SupertonicBuffer()
        buffer.generated(2_000, 2_500, 10_000)
        assertFalse(buffer.ready(4f, 8_000, 4, false, 12_000))
        assertTrue(buffer.ready(4f, 8_000, 4, false, 18_000))
        assertTrue(buffer.ready(4f, 2_000, 1, true, 10_000))
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
}
