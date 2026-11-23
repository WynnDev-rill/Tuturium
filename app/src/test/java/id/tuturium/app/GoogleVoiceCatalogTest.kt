package id.tuturium.app

import org.junit.Assert.*
import org.junit.Test

class GoogleVoiceCatalogTest {
    @Test fun onlyIndonesianGoogleVoicesAreOffered() {
        val available = listOf(
            VoiceChoice("id-id-x-dfz-local", "id", "Indonesian", 400),
            VoiceChoice("id-id-x-idc-local", "id", "Indonesian", 400),
            VoiceChoice("id-id-x-idd-local", "id", "Indonesian", 400),
            VoiceChoice("id-id-x-ide-local", "id", "Indonesian", 400),
            VoiceChoice("en-us-x-sfg-local", "en", "English", 400),
            VoiceChoice("fr-fr-x-vlf-local", "fr", "French", 400))
        val filtered = googleIndonesianVoices(available)
        assertEquals(4, filtered.size)
        assertTrue(filtered.all { isIndonesian(it.language) })
        assertEquals("Suara Indonesia 1", indonesianVoiceLabel(filtered[0], filtered))
        assertEquals("Suara Indonesia 4", indonesianVoiceLabel(filtered[3], filtered))
    }

    @Test fun aStaleSelectedVoiceMustNeverForceUnrelatedLanguage() {
        val voices = listOf(
            VoiceChoice("en-us-x-z", "en", "English", 500),
            VoiceChoice("id-id-x-idd-local", "id", "Indonesian", 300))
        assertEquals("id-id-x-idd-local",
            resolveIndonesianVoice(voices, "old-no-longer-installed")?.name)
        assertNull(resolveIndonesianVoice(listOf(voices.first()), null))
    }

    @Test fun selectedOnlineVoiceCanBeRejectedUnlessOptedIn() {
        val voices = listOf(
            VoiceChoice("id-online", "id", "Indonesia", 500, true),
            VoiceChoice("id-local", "id", "Indonesia", 300, false))
        assertEquals("id-local",
            resolveIndonesianVoice(voices, "id-online", allowNetwork = false)?.name)
    }
}
