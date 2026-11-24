package id.tuturium.app

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class NarrationAudioTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun boostsQuietNarrationWhileRoundingOffLoudPeaks() {
        val samples = floatArrayOf(0f, 0.03f, -0.06f, Float.NaN)
        val gain = narrationGain(samples)
        assertEquals(4f, gain)
        assertEquals(0.12f, amplifyNarration(samples[1], gain), 0.0001f)
        assertEquals(-0.24f, amplifyNarration(samples[2], gain), 0.0001f)
        assertEquals(0f, amplifyNarration(0f, gain), 0f)
        assertEquals(0f, amplifyNarration(Float.NaN, gain), 0f)
        val peak = amplifyNarration(1f, gain)
        assertTrue(peak in 0.8f..<0.96f)
        assertEquals(-peak, amplifyNarration(-1f, gain), 0f)
    }

    @Test fun cachePersistsAudioWithCuesAndEvictsUnderBudget() {
        val directory = temporary.newFolder()
        val source = temporary.newFile()
        val target = temporary.newFile()
        val bytes = ByteArray(300) { it.toByte() }
        source.writeBytes(bytes)
        val audio = SynthesizedAudio(listOf(AudioCue(0, 0..3), AudioCue(50, 5..7)), 100)
        NarrationAudioCache(directory, 400).save("Baca ini", "voice-v1", source, audio)
        val reopened = NarrationAudioCache(directory, 400)
        assertEquals(audio.copy(cacheHit = true), reopened.restore("Baca ini", "voice-v1", target))
        assertArrayEquals(bytes, target.readBytes())
        assertNull(reopened.restore("Baca ini", "voice-v2", target))
        reopened.save("Baca itu", "voice-v1", source, audio)
        assertNull(reopened.restore("Baca ini", "voice-v1", target))
        assertEquals(audio.copy(cacheHit = true), reopened.restore("Baca itu", "voice-v1", target))
        assertTrue(directory.listFiles()!!.sumOf { it.length() } <= 400)
        directory.listFiles()!!.single().writeBytes(byteArrayOf(0))
        assertNull(reopened.restore("Baca itu", "voice-v1", target))
    }
}
