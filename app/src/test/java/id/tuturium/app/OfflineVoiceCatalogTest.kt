package id.tuturium.app
import org.junit.Assert.*
import org.junit.Test
class OfflineVoiceCatalogTest {
    @Test fun everyVoiceHasAUniqueIdAndARealDownload() {
        val voices = OfflineCatalog.models.flatMap { it.voices }
        assertEquals(voices.size, voices.map { it.id }.distinct().size)
        assertEquals(10, OfflineCatalog.models.first { it.id == "supertonic3" }.voices.size)
        assertEquals(1, OfflineCatalog.models.first { it.id == "piper" }.voices.size)
        assertTrue(OfflineCatalog.models.any { it.id == "arbo-nano" })
        assertTrue(OfflineCatalog.models.all { it.files.isNotEmpty() && it.files.all { f -> f.url.startsWith("https://") } })
    }
    @Test fun noUnknownModelCanBeSelected() {
        assertNull(OfflineCatalog.voice("offline:unknown"))
        assertNull(OfflineCatalog.voice("id-id-x-idd-local"))
        assertNotNull(OfflineCatalog.voice("offline:supertonic3:0"))
    }
}
