package id.tuturium.app

import org.junit.Assert.*
import org.junit.Test

class SpeechControlTest {
    @Test fun autoRampingEveryFiveHundredWordsCappedAtFour() {
        assertEquals(1.0f, rampedSpeed(1f, 499, true), 0.0001f)
        assertEquals(1.1f, rampedSpeed(1f, 500, true), 0.0001f)
        assertEquals(1.2f, rampedSpeed(1f, 1000, true), 0.0001f)
        assertEquals(4f, rampedSpeed(3.8f, 2000, true), 0.0001f)
        assertEquals(1f, rampedSpeed(1f, 2000, false), 0.0001f)
    }

    @Test fun inlineSkippedContentPreservesOriginalOffsets() {
        val options = ListeningOptions(skipEnabled = true, skipUrls = true, skipRound = true)
        val raw = "Baca (catatan) teks https://contoh.com berikutnya."
        val result = prepareSpeech(raw, options)
        assertFalse(result.text.contains("catatan"))
        assertFalse(result.text.contains("https"))
        val idx = result.text.indexOf("berikutnya")
        assertEquals(raw.indexOf("berikutnya"), result.offsets[idx])
    }

    @Test fun disableSkipKeepsAllTheText() {
        val raw = "Tulisan (penting) https://contoh.com"
        assertEquals(raw, prepareSpeech(raw, ListeningOptions()).text)
    }

    @Test fun filteringDoesNotRemoveOrdinaryProse() {
        val options = ListeningOptions(skipEnabled = true, skipHeaders = true,
            skipFootnotes = true, skipFormulas = true)
        assertEquals("Dia membaca rumus dengan teliti.",
            prepareSpeech("Dia membaca rumus dengan teliti.", options).text)
    }

    @Test fun tenSecondSeekUsesCurrentSpeechRate() {
        val passes = (1..10).map { Pass("satu dua tiga empat lima", 0, 0, 0) }
        assertEquals(6, seekBySeconds(passes, 1, 10, 1f))
        assertEquals(1, seekBySeconds(passes, 6, -10, 1f))
    }
    @Test fun voicePreviewSuccessAllowsOneSafeRetryAtNormalSpeed() {
        assertTrue(shouldRetrySpeechAtNormalSpeed(2f, false, true))
        assertTrue(shouldRetrySpeechAtNormalSpeed(0.8f, false, true))
        assertFalse(shouldRetrySpeechAtNormalSpeed(1f, false, true))
        assertFalse(shouldRetrySpeechAtNormalSpeed(2f, true, true))
        assertFalse(shouldRetrySpeechAtNormalSpeed(2f, false, false))
    }

    @Test fun speechPreparationFallsBackToOriginalTextWhenFilterThrows() {
        val text = "Bab Satu MENEMUKAN SUARAKU Meskipun bahasa Inggris adalah bahasa kedua."
        val result = safePreparedSpeech(text, ListeningOptions(skipEnabled = true)) { _, _ ->
            throw IllegalArgumentException("OEM-specific filter failure")
        }
        assertEquals(text, result.text)
        assertArrayEquals(IntArray(text.length) { it }, result.offsets)
    }

    @Test fun speechPreparationKeepsOriginalPositionMappingForNormalText() {
        val text = "Aku mempelajari bahasa ibuku."
        val result = safePreparedSpeech(text, ListeningOptions())
        assertEquals(text, result.text)
        assertEquals(4, result.offsets[4])
    }

}
