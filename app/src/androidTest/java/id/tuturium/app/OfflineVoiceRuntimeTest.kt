package id.tuturium.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
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
                    OfflineSynth.write(context, voice, "Selamat pagi, Indonesia. Mari membaca buku.", wav)
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
    @Test(timeout = 240_000L)
    fun highRatesPlayEveryQueuedPassThroughToItsLastCue() = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("offlineVoiceSmoke") == "true")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val model = OfflineCatalog.models.single()
        OfflineModelStore.install(context, model) {}
        val passages = listOf(
            "Kalimat pertama dibaca sampai kata terakhir.",
            "Kalimat kedua menyebutkan perpustakaan dan halaman baru.",
            "Kalimat terakhir selesai dengan kata penutup."
        ).mapIndexed { index, text -> OfflinePass(index, PreparedSpeech(text, IntArray(text.length) { it })) }
        for (rate in listOf(2f, 4f)) withContext(Dispatchers.Main) {
            val engine = OfflineAudio(context)
            val started = mutableListOf<Int>()
            val spoken = mutableListOf<Int>()
            val lastCue = mutableMapOf<Int, IntRange>()
            var interruptions = 0
            val done = CompletableDeferred<Unit>()
            try {
                engine.play(this, model.voices.first(), passages.asSequence(), rate, 1f,
                    onPass = { started += it.index },
                    onSpoken = { spoken += it.index },
                    onRange = { range -> started.lastOrNull()?.let { lastCue[it] = range } },
                    onRendering = { if (!it && started.isNotEmpty()) interruptions++ },
                    onDone = { done.complete(Unit) },
                    onError = { done.completeExceptionally(it) })
                withTimeout(80_000) { done.await() }
                assertEquals("Start order at ${rate}x", listOf(0, 1, 2), started)
                assertEquals("Completion order at ${rate}x", started, spoken)
                assertEquals("Every passage reached its last cue at ${rate}x",
                    passages.associate { it.index to 0..it.speech.text.lastIndex }, lastCue)
                assertEquals("Visible rebuffering at ${rate}x", 0, interruptions)
            } finally {
                engine.stop()
            }
        }
    }
}
