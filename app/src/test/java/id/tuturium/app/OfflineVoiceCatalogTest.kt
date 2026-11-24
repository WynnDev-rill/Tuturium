package id.tuturium.app
import org.junit.Assert.*
import org.junit.Test
class OfflineVoiceCatalogTest {
    @Test fun everyVoiceHasAUniqueIdAndARealDownload() {
        val voices = OfflineCatalog.models.flatMap { it.voices }
        assertEquals(voices.size, voices.map { it.id }.distinct().size)
        assertEquals(10, OfflineCatalog.models.first { it.id == "supertonic3" }.voices.size)
        assertEquals(listOf("supertonic3"), OfflineCatalog.models.map { it.id })
        assertTrue(OfflineCatalog.models.all { it.files.isNotEmpty() && it.files.all { f -> f.url.startsWith("https://") && f.size > 0 && f.sha256.matches(Regex("[0-9a-f]{64}")) } })
    }
    @Test fun noUnknownModelCanBeSelected() {
        assertNull(OfflineCatalog.voice("offline:unknown"))
        assertNull(OfflineCatalog.supportedSelection("offline:piper:0"))
        assertNull(OfflineCatalog.supportedSelection("offline:arbo-nano:0"))
        assertEquals("id-id-x-idd-local", OfflineCatalog.supportedSelection("id-id-x-idd-local"))
        assertEquals("offline:supertonic3:0", OfflineCatalog.supportedSelection("offline:supertonic3:0"))
        assertNull(OfflineCatalog.voice("id-id-x-idd-local"))
        assertNotNull(OfflineCatalog.voice("offline:supertonic3:0"))
    }
    @Test fun synthesisRangesBoundLongTokensAndKeepSurrogatePairsWhole() {
        val text = "kalimat pertama " + "a".repeat(500) + "🌻" + " akhir"
        val ranges = synthesisRanges(text, 17)
        assertTrue(ranges.all { it.count() in 1..17 })
        assertTrue(ranges.all { !text[it.first].isLowSurrogate() && !text[it.last].isHighSurrogate() })
        assertEquals(text.filterNot(Char::isWhitespace),
            ranges.joinToString("") { text.substring(it) }.filterNot(Char::isWhitespace))
    }
}
