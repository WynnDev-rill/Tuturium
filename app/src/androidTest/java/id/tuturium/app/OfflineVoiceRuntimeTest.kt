package id.tuturium.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineVoiceRuntimeTest {
    @Test(timeout = 900000L)
    fun everyDownloadableIndonesianVoiceProducesNonemptyWav() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        // The slow network/inference check runs only when CI explicitly requests it.
        assumeTrue(InstrumentationRegistry.getArguments().getString("offlineVoiceSmoke") == "true")
        for (pack in OfflineCatalog.models) {
            OfflineModelStore.install(context, pack) {}
            assertTrue("Model not installed: " + pack.id, OfflineModelStore.installed(context, pack))
            for (voice in pack.voices) {
                val wav = File.createTempFile("offline-smoke-", ".wav", context.cacheDir)
                try {
                    OfflineSynth.write(context, voice, "Selamat pagi, Indonesia. Mari membaca buku.", 1.6f, wav)
                    val bytes = wav.readBytes()
                    assertTrue(voice.label + " produced no audio", bytes.size > 2000)
                    assertEquals("RIFF", String(bytes, 0, 4, Charsets.US_ASCII))
                    assertEquals("WAVE", String(bytes, 8, 4, Charsets.US_ASCII))
                    val pcm = ByteBuffer.wrap(bytes, 44, bytes.size - 44).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
                    var peak = 0
                    while (pcm.hasRemaining()) peak = maxOf(peak, kotlin.math.abs(pcm.get().toInt()))
                    assertTrue(voice.label + " generated silence", peak > 100)
                } finally { wav.delete() }
            }
        }
    }
}
