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

    @Test fun selectedIndonesianOnlineVoiceIsAllowedByDefault() {
        val voices = listOf(
            VoiceChoice("en-us", "en", "English", 500, true),
            VoiceChoice("id-id-network", "id", "Indonesia", 400, true)
        )
        assertEquals("id-id-network", resolveIndonesianVoice(voices, "id-id-network")?.name)
        assertEquals("id-id-network", resolveIndonesianVoice(voices, null)?.name)
        assertNull(resolveIndonesianVoice(voices, null, allowNetwork = false))
    }

    @Test fun localVoiceRemainsPreferredButOnlineIsSelectable() {
        val voices = listOf(
            VoiceChoice("id-id-online", "id", "Indonesia", 600, true),
            VoiceChoice("id-id-x-idd-local", "id", "Indonesia", 300, false)
        )
        assertEquals("id-id-x-idd-local", resolveIndonesianVoice(voices, null)?.name)
        assertEquals("id-id-online", resolveIndonesianVoice(voices, "id-id-online")?.name)
        assertEquals(listOf(false, true),
            googleIndonesianVoices(voices).map { it.network })
    }

    @Test fun automaticGoogleIndonesianFallbackStaysIndonesian() {
        val voices = listOf(
            VoiceChoice(GOOGLE_ID_AUTO, "id", "Indonesia", 0, true),
            VoiceChoice("en-fallback", "en", "English", 500, true)
        )
        assertEquals("Google Indonesia otomatis", indonesianVoiceLabel(voices.first(), voices))
        assertEquals(GOOGLE_ID_AUTO, resolveIndonesianVoice(voices, GOOGLE_ID_AUTO)?.name)
        assertTrue(googleIndonesianVoices(voices).all { it.language == "id" })
    }
}
