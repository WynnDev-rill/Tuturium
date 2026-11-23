package id.tuturium.app

import org.junit.Assert.*
import org.junit.Test

class ReadingModelTest {
    @Test fun sentencesKeepTheirOffsetsAndSectionOrder() {
        val document = ReadingDocument(listOf(
            Section("Awal", "Pagi ini hujan. Besok cerah.\n\nParagraf baru."),
            Section("Akhir", "Selesai.")
        ))
        assertEquals(listOf("Pagi ini hujan.", "Besok cerah.", "Paragraf baru.", "Selesai."),
            document.passes.map { it.text })
        assertEquals(listOf(0, 16, 0, 0), document.passes.map { it.offset })
        assertEquals(listOf(0, 0, 1, 0), document.passes.map { it.paragraph })
        assertEquals(listOf(0, 0, 0, 1), document.passes.map { it.section })
    }

    @Test fun chunkingMaintainsUtf16BoundariesWithinEngineLimit() {
        val text = "A 😀😀😀😀 ."
        val result = splitSpeech(text, limit = 4)
        assertTrue(result.all { it.text.length <= 4 })
        assertEquals(text.replace(" ", ""), result.joinToString("") { it.text }.replace(" ", ""))
        result.forEach {
            assertEquals(it.text, text.substring(it.offset, it.offset + it.text.length))
            assertFalse(it.text.first().isLowSurrogate())
            assertFalse(it.text.last().isHighSurrogate())
        }
    }

    @Test fun voicePreferenceRespectsExplicitChoiceAndIndonesianLocalVoice() {
        val english = VoiceChoice("en-GB", "en", "English", 500)
        val indonesian = VoiceChoice("id-id-1", "id", "Indonesia", 500)
        val local = VoiceChoice("local(idd)", "id", "Indonesia", 200)
        val list = listOf(english, indonesian, local)
        assertEquals(local, chooseVoice(list, null, "en"))
        assertEquals(english, chooseVoice(list, english.name, "id"))
        assertEquals(indonesian, chooseVoice(listOf(indonesian, english), "missing", "en"))
        val alias = VoiceChoice("Indonesian", "in", "Indonesia", 300)
        assertEquals(alias, chooseVoice(listOf(english, alias), null, "en"))
        assertNull(chooseVoice(emptyList(), null, "id"))
    }

    @Test fun updatesOnlyAcceptNewerStableVersions() {
        assertTrue(isNewer("v1.10.0", "1.9.8"))
        assertTrue(isNewer("v2.0.0", "1.99.99"))
        assertFalse(isNewer("v1.9.8", "1.9.8"))
        assertFalse(isNewer("v1.9.7", "1.9.8"))
        assertFalse(isNewer("v2.0.0-beta.1", "1.9.8"))
        assertFalse(isNewer("../v2.0.0", "1.9.8"))
        assertFalse(isNewer("v99999999999.0.0", "1.9.8"))
    }
}
