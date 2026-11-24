package id.tuturium.app

import org.junit.Assert.*
import org.junit.Test

class SpeechControlTest {
    @Test fun autoRampingEverySixHundredWordsPreservesTheBaseAndCapsAtFour() {
        assertEquals(1.25f, rampedSpeed(1.25f, 599, true), 0.0001f)
        assertEquals(1.30f, rampedSpeed(1.25f, 600, true), 0.0001f)
        assertEquals(1.35f, rampedSpeed(1.25f, 1200, true), 0.0001f)
        assertEquals(4f, rampedSpeed(3.95f, 2000, true), 0.0001f)
        assertEquals(1.25f, rampedSpeed(1.25f, 2000, false), 0.0001f)
    }

    @Test fun aNewBaseOrToggleResetsCompletedWordsButReapplyingTheSameSettingsKeepsProgress() {
        val speed = AutomaticSpeed()
        speed.spoken(600)
        speed.configure(1.25f, true)
        speed.spoken(420)
        speed.configure(1.25f, true)
        speed.spoken(180)
        assertEquals(1.30f, speed.rate, 0.0001f)
        speed.configure(1.25f, false)
        speed.spoken(1000)
        speed.configure(1.25f, true)
        assertEquals(1.25f, speed.rate, 0.0001f)
        speed.spoken(Int.MAX_VALUE)
        assertEquals(4f, speed.rate, 0.0001f)
        speed.configure(2f, true)
        assertEquals(2f, speed.rate, 0.0001f)
        speed.spoken(600)
        speed.reset()
        assertEquals(2f, speed.rate, 0.0001f)
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

    @Test fun skipFiltersSeeTheEntireParagraphBeforeSentenceSegmentation() {
        val raw = "Pertama (catatan satu. Catatan kedua.) dibaca. Lanjut https://contoh.com/cerita."
        val book = ReadingDocument(listOf(Section("Bab", raw)))
        val paragraph = prepareSpeech(raw, ListeningOptions(skipEnabled = true))
        val slices = book.passes.map(paragraph::forPass)
        assertTrue(slices.joinToString(" ") { it.text }.contains("dibaca"))
        assertFalse(slices.joinToString(" ") { it.text }.contains("Catatan"))
        assertFalse(slices.joinToString(" ") { it.text }.contains("contoh.com"))
        slices.zip(book.passes).forEach { (prepared, pass) ->
            prepared.text.forEachIndexed { i, c -> assertEquals(c, pass.text[prepared.offsets[i]]) }
        }
    }

    @Test fun skipFootnotesPreservesNumberedListsAndSkipsExplicitNotes() {
        val options = ListeningOptions(skipEnabled = true)
        assertEquals("1. Beli buku.", prepareSpeech("1. Beli buku.", options).text)
        assertEquals("", prepareSpeech("[1] Sumber kutipan.", options).text)
    }

}
