package id.tuturium.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
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
                    val phrase = "Selamat pagi, Indonesia. Mari membaca buku."
                    val coldAt = android.os.SystemClock.elapsedRealtime()
                    val first = OfflineSynth.write(context, voice, phrase, wav)
                    val firstMs = android.os.SystemClock.elapsedRealtime() - coldAt
                    val bytes = wav.readBytes()
                    assertTrue(voice.label + " produced no audio", bytes.size > 2000)
                    assertEquals("RIFF", String(bytes, 0, 4, Charsets.US_ASCII))
                    assertEquals("WAVE", String(bytes, 8, 4, Charsets.US_ASCII))
                    val pcm = ByteBuffer.wrap(bytes, 44, bytes.size - 44).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
                    var peak = 0
                    while (pcm.hasRemaining()) peak = maxOf(peak, kotlin.math.abs(pcm.get().toInt()))
                    assertTrue(voice.label + " generated silence", peak > 100)
                    assertTrue(voice.label + " exceeded the limiter ceiling", peak < 31457)
                    val repeatAt = android.os.SystemClock.elapsedRealtime()
                    val repeated = OfflineSynth.write(context, voice, phrase, wav)
                    val repeatMs = android.os.SystemClock.elapsedRealtime() - repeatAt
                    android.util.Log.i("TuturiumVoiceBenchmark",
                        "${voice.id} first_ms=$firstMs cache_ms=$repeatMs duration_ms=${first.durationMs}")
                    assertEquals(first.copy(cacheHit = true), repeated)
                    assertArrayEquals(bytes, wav.readBytes())
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
            "Pada kalimat kedua kami membaca tentang perpustakaan di pinggir kota, menyusuri rak " +
                "buku bersama teman dari sekolah, lalu mencari sebuah cerita lama tentang perjalanan " +
                "menuju pulau kecil yang ditumbuhi pohon kelapa dan dipenuhi burung berwarna cerah.",
            "Kalimat terakhir selesai dengan kata penutup."
        ).mapIndexed { index, text -> OfflinePass(index, PreparedSpeech(text, IntArray(text.length) { it })) }
        for (rate in listOf(2f, 4f)) withContext(Dispatchers.Main) {
            val engine = OfflineAudio(context)
            val started = mutableListOf<Int>()
            val spoken = mutableListOf<Int>()
            val clipDurations = mutableListOf<Long>()
            val lastCue = mutableMapOf<Int, IntRange>()
            var interruptions = 0
            val done = CompletableDeferred<Unit>()
            try {
                engine.play(this, model.voices.first(), passages.asSequence(), rate, 1f,
                    onPass = { started += it.index },
                    onSpoken = { pass, duration ->
                        spoken += pass.index
                        clipDurations += duration
                    },
                    onRange = { range -> started.lastOrNull()?.let { lastCue[it] = range } },
                    onRendering = { if (!it && started.isNotEmpty()) interruptions++ },
                    onDone = { done.complete(Unit) },
                    onError = { done.completeExceptionally(it) })
                withTimeout(80_000) { done.await() }
                assertEquals("Start order at ${rate}x", listOf(0, 1, 2), started)
                assertEquals("Completion order at ${rate}x", started, spoken)
                assertTrue("Played narration must report the full clip duration at ${rate}x",
                    clipDurations.size == passages.size && clipDurations.all { it > 0L })
                assertEquals("Every passage reached its last cue at ${rate}x",
                    passages.associate { it.index to synthesisRanges(it.speech.text).last() }, lastCue)
                assertEquals("Visible rebuffering at ${rate}x", 0, interruptions)
            } finally {
                engine.stop()
            }
        }
    }

    @Test(timeout = 240_000L)
    fun opusCacheKeepsNarrationCuesAndPlaysThroughMedia3AtFourTimesSpeed() = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("offlineVoiceSmoke") == "true")
        assumeTrue(android.os.Build.VERSION.SDK_INT >= 29)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val pack = OfflineCatalog.models.single()
        OfflineModelStore.install(context, pack) {}
        val voice = pack.voices.first()
        val text = "Di dermaga, penjaga kapal menyerahkan daftar penumpang kepada petugas. " +
            "Setelah memeriksa karcis, ia membuka pintu peron dan mendampingi keluarga dengan kursi roda " +
            "sampai mereka menemukan tempat duduk di dekat jendela yang menghadap laut."
        val wav = File.createTempFile("codec-smoke-", ".wav", context.cacheDir)
        try {
            val firstAt = android.os.SystemClock.elapsedRealtime()
            val fresh = OfflineSynth.write(context, voice, text, wav, steps = 5, compressCache = true)
            val firstMs = android.os.SystemClock.elapsedRealtime() - firstAt
            assertFalse(fresh.opus)
            val pcmSize = wav.length()
            val replayAt = android.os.SystemClock.elapsedRealtime()
            val cached = OfflineSynth.write(context, voice, text, wav, steps = 5)
            val replayMs = android.os.SystemClock.elapsedRealtime() - replayAt
            android.util.Log.i("TuturiumVoiceBenchmark", "opus_first_ms=$firstMs opus_cache_ms=$replayMs " +
                "pcm_bytes=$pcmSize opus_bytes=${wav.length()} duration_ms=${fresh.durationMs}")
            assertEquals(fresh.copy(cacheHit = true, opus = true), cached)
            assertEquals("OggS", wav.inputStream().use { String(ByteArray(4).also { header -> java.io.DataInputStream(it).readFully(header) }, Charsets.US_ASCII) })
            assertTrue("Opus should use less than one fifth of PCM storage", wav.length() * 5 < pcmSize)
            withContext(Dispatchers.Main) {
                val engine = OfflineAudio(context)
                val pass = OfflinePass(0, PreparedSpeech(text, IntArray(text.length) { it }))
                val done = CompletableDeferred<Unit>()
                var completed = 0
                try {
                    engine.play(this, voice, sequenceOf(pass), 4f, 1f, onPass = {}, onRange = {},
                        onSpoken = { _, _ -> completed++ }, onRendering = {},
                        onDone = { done.complete(Unit) }, onError = { done.completeExceptionally(it) })
                    withTimeout(40_000L) { done.await() }
                    assertEquals(1, completed)
                } finally {
                    engine.stop()
                }
            }
        } finally {
            wav.delete()
        }
    }

    @Test(timeout = 240_000L)
    fun offlineForegroundServiceAdvancesWithAnotherAppOpenAndAirplaneModeOn() = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("offlineVoiceSmoke") == "true")
        assumeTrue(android.os.Build.VERSION.SDK_INT >= 29)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val pack = OfflineCatalog.models.single()
        OfflineModelStore.install(context, pack) {}
        val voice = pack.voices.first()
        val previous = ReadingSettings.read(context)
        val options = ListeningOptions.read(context)
        val document = ReadingDocument(listOf(Section("Uji",
            "Nina membaca berita pagi. Rani menutup pintu. Mereka menunggu kereta. " +
                "Kereta bergerak perlahan. Penjaga menyalakan lampu. Semua penumpang tiba dengan selamat.")))
        val library = Library.get(context)
        val book = library.add("Uji narasi latar", "TXT", document)
        fun shell(command: String): String =
            android.os.ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(command))
                .bufferedReader().use { it.readText() }
        val alreadyOffline = shell("settings get global airplane_mode_on").trim() == "1"
        try {
            if (!alreadyOffline) shell("cmd connectivity airplane-mode enable")
            assertEquals("1", shell("settings get global airplane_mode_on").trim())
            previous.copy(voice = voice.id, speed = 1f).save(context)
            ListeningOptions().save(context)
            androidx.test.core.app.ActivityScenario.launch(MainActivity::class.java).use { activity ->
                activity.onActivity {
                    assertTrue(ReadingService.command(it, ReadingService.PLAY, book.id, 0))
                }
                val first = withTimeout(45_000L) {
                    ReadingService.state.first { it.bookId == book.id && it.playing }
                }
                val opened = shell("am start -W -a android.settings.SETTINGS")
                assertTrue(opened, opened.contains("Status: ok"))
                val backgroundIndex = maxOf(first.index, ReadingService.state.value.index)
                val later = withTimeout(30_000L) {
                    ReadingService.state.first { it.bookId == book.id && it.playing && it.index > backgroundIndex }
                }
                assertTrue(later.index > backgroundIndex)
            }
        } finally {
            ReadingService.command(context, ReadingService.STOP)
            previous.save(context)
            options.save(context)
            if (!alreadyOffline) shell("cmd connectivity airplane-mode disable")
            library.delete(book.id)
            library.deletePermanently(book.id)
        }
    }

    @Test(timeout = 30_000L)
    fun recoversFromAnExhaustedQueueWithoutSkippingOrRepeating() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sampleRate = 24_000
        val pcmBytes = sampleRate * 2
        val pcm = ByteBuffer.allocate(44 + pcmBytes).order(ByteOrder.LITTLE_ENDIAN)
            .put("RIFF".toByteArray()).putInt(36 + pcmBytes).put("WAVEfmt ".toByteArray())
            .putInt(16).putShort(1).putShort(1).putInt(sampleRate).putInt(sampleRate * 2)
            .putShort(2).putShort(16).put("data".toByteArray()).putInt(pcmBytes).array()
        val texts = (0..6).map { "Kalimat nomor $it" }
        val passages = texts.mapIndexed { index, text ->
            OfflinePass(index, PreparedSpeech(text, IntArray(text.length) { it }))
        }
        withContext(Dispatchers.Main) {
            val engine = OfflineAudio(context) { _, _, text, target, _, _, _ ->
                if (text == texts[4]) delay(3_000)
                target.writeBytes(pcm)
                SynthesizedAudio(listOf(AudioCue(0, text.indices)), 1_000)
            }
            val started = mutableListOf<Int>()
            val spoken = mutableListOf<Int>()
            var interrupted = false
            val done = CompletableDeferred<Unit>()
            try {
                engine.play(this, OfflineCatalog.models.single().voices.first(), passages.asSequence(), 4f, 1f,
                    onPass = { started += it.index }, onSpoken = { pass, _ -> spoken += pass.index },
                    onRange = {}, onRendering = { if (!it && started.isNotEmpty()) interrupted = true },
                    onDone = { done.complete(Unit) }, onError = { done.completeExceptionally(it) })
                withTimeout(15_000) { done.await() }
                assertTrue("Fixture should exhaust the initial buffer", interrupted)
                assertEquals(passages.indices.toList(), started)
                assertEquals(started, spoken)
            } finally {
                engine.stop()
            }
        }
    }
}
