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
        assertTrue(gain > 4f && gain <= 8f)
        assertEquals(samples[1] * gain, amplifyNarration(samples[1], gain), 0.0001f)
        assertEquals(samples[2] * gain, amplifyNarration(samples[2], gain), 0.0001f)
        assertEquals(0f, amplifyNarration(0f, gain), 0f)
        assertEquals(0f, amplifyNarration(Float.NaN, gain), 0f)
        val peak = amplifyNarration(1f, gain)
        assertTrue(peak in 0.8f..<0.96f)
        assertEquals(-peak, amplifyNarration(-1f, gain), 0f)
    }

    @Test fun opusInputResamplesAcrossInputBufferBoundaries() {
        val original = java.nio.ByteBuffer.allocate(6).order(java.nio.ByteOrder.LITTLE_ENDIAN)
            .putShort(0).putShort(12_000).putShort(24_000).array()
        val resampler = NarrationResampler(java.io.DataInputStream(original.inputStream()),
            3, 24_000, 48_000)
        val destination = java.nio.ByteBuffer.allocate(12).order(java.nio.ByteOrder.LITTLE_ENDIAN)
        assertEquals(2, resampler.write(destination, maxFrames = 2))
        assertEquals(4, resampler.write(destination, maxFrames = 5))
        assertEquals(0, resampler.write(destination, maxFrames = 5))
        destination.rewind()
        val shorts = destination.asShortBuffer()
        assertArrayEquals(shortArrayOf(0, 6_000, 12_000, 18_000, 24_000, 24_000),
            ShortArray(6) { shorts.get() })
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
