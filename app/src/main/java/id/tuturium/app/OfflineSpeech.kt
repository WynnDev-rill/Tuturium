package id.tuturium.app

import android.content.Context
import com.k2fsa.sherpa.onnx.*
import kotlinx.coroutines.*
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Sherpa has its own statically linked ONNX runtime. Inference stays on one worker. */
internal object OfflineSynth {
    private val dispatcher = Dispatchers.IO.limitedParallelism(1)
    private var sherpa: OfflineTts? = null
    private var retryOpusAfter = 0L

    private fun load(context: Context): OfflineTts {
        sherpa?.let { return it }
        val model = OfflineCatalog.models.single()
        check(OfflineModelStore.installed(context, model)) { "Unduh model Supertonic terlebih dahulu." }
        val dir = OfflineModelStore.directory(context, model)
        fun path(file: String) = File(dir, file).absolutePath
        return OfflineTts(config = OfflineTtsConfig(model = OfflineTtsModelConfig(
            supertonic = OfflineTtsSupertonicModelConfig(
                durationPredictor = path("duration_predictor.int8.onnx"),
                textEncoder = path("text_encoder.int8.onnx"),
                vectorEstimator = path("vector_estimator.int8.onnx"),
                vocoder = path("vocoder.int8.onnx"), ttsJson = path("tts.json"),
                unicodeIndexer = path("unicode_indexer.bin"), voiceStyle = path("voice.bin")),
            numThreads = synthesisThreadCount(Runtime.getRuntime().availableProcessors())))).also { sherpa = it }
    }

    private val lifetime = CoroutineScope(SupervisorJob() + dispatcher)
    private var releaseJob: Job? = null
    private var sessions = 0

    suspend fun withWarmModel(block: suspend () -> Unit) {
        var retained = false
        try {
            withContext(dispatcher) {
                sessions++
                retained = true
                releaseJob?.cancel()
            }
            block()
        } finally {
            if (retained) withContext(NonCancellable) {
                withContext(dispatcher) {
                    sessions--
                    releaseWhenIdle()
                }
            }
        }
    }

    private fun releaseWhenIdle() {
        if (sessions != 0) return
        releaseJob?.cancel()
        releaseJob = lifetime.launch {
            delay(45_000)
            runCatching { sherpa?.release() }
            sherpa = null
        }
    }

    suspend fun prewarm(context: Context) = withContext(dispatcher) {
        if (android.os.Build.VERSION.SDK_INT >= 29 &&
            context.getSystemService(android.os.PowerManager::class.java).currentThermalStatus >=
            android.os.PowerManager.THERMAL_STATUS_MODERATE
        ) return@withContext
        if (OfflineModelStore.installed(context, OfflineCatalog.models.single())) {
            try {
                runCatching { load(context) }
            } finally {
                releaseWhenIdle()
            }
        }
    }

    /** Prepare only a small startup window while the reader is paused. The normal
     * playback pipeline reuses the exact same cache, without starting a player. */
    suspend fun prepareOpeningAudio(context: Context, voice: OfflineVoice, document: ReadingDocument,
        index: Int, offset: Int, options: ListeningOptions, playbackRate: Float) = withContext(Dispatchers.IO) {
        if (!OfflineModelStore.installed(context, OfflineCatalog.model(voice.model))) return@withContext
        if (android.os.Build.VERSION.SDK_INT >= 29 &&
            context.getSystemService(android.os.PowerManager::class.java).currentThermalStatus >=
            android.os.PowerManager.THERMAL_STATUS_MODERATE) return@withContext
        val steps = SupertonicBuffer().inferenceSteps(playbackRate, 0, filling = true, thermal = 0)
        withWarmModel {
            for (i in index until minOf(index + 2, document.passes.size)) {
                ensureActive()
                val pass = document.passes[i]
                val prepared = if (options.skipEnabled)
                    safePreparedSpeech(document.paragraphs[pass.section][pass.paragraph], options).forPass(pass)
                else prepareSpeech(pass.text, options)
                val start = if (i == index && offset > 0)
                    prepared.offsets.indexOfFirst { it >= offset } else 0
                if (start < 0 || start >= prepared.text.length) continue
                val text = prepared.text.substring(start)
                if (text.isBlank()) continue
                val temp = File.createTempFile("prefetch-", ".wav", context.cacheDir)
                try {
                    write(context, voice, text, temp, steps, pauseAfterMs = document.pauseAfter(i, options).totalMs)
                } finally {
                    temp.delete()
                }
            }
        }
    }

    suspend fun write(context: Context, voice: OfflineVoice, text: String, target: File,
        steps: Int = 8, compressCache: Boolean = false, pauseAfterMs: Int = 0): SynthesizedAudio =
        withContext(dispatcher) {
            releaseJob?.cancel()
            try {
                val cache = NarrationAudioCache(File(context.cacheDir, "supertonic-audio"))
                val model = OfflineCatalog.model(voice.model)
                fun key(quality: Int) = "${model.files.single().sha256}/${voice.id}/id/$quality/gain-v2/flow-v1/$pauseAfterMs"
                if (OfflineModelStore.installed(context, model)) {
                    for (candidate in listOf(8, 6, 5)) {
                        if (candidate < steps) break
                        cache.restore(text, key(candidate), target)?.let { return@withContext it }
                    }
                }
                var rate = 0
                var bytes = 0L
                val cues = mutableListOf<AudioCue>()
                val buffer = ByteBuffer.allocate(16384).order(ByteOrder.LITTLE_ENDIAN)
                java.io.RandomAccessFile(target, "rw").use { wav ->
                    wav.setLength(0)
                    wav.write(ByteArray(44))
                    val engine = load(context)
                    check(voice.speaker in 0 until engine.numSpeakers())
                    val ranges = synthesisRanges(text)
                    for ((index, range) in ranges.withIndex()) {
                        ensureActive()
                        val raw = text.substring(range)
                        val normalized = PreparedSpeech(raw, IntArray(raw.length) { it }).normalizedForNarration().text
                        val pcm = engine.generateWithConfig(normalized, GenerationConfig(
                            sid = voice.speaker, speed = 1f, numSteps = steps, extra = mapOf("lang" to "id")))
                        ensureActive()
                        check(pcm.samples.isNotEmpty() && pcm.sampleRate > 0)
                        if (rate == 0) rate = pcm.sampleRate
                        check(rate == pcm.sampleRate)
                        cues += AudioCue(bytes * 500L / rate, range)
                        buffer.clear()
                        val gain = narrationGain(pcm.samples)
                        val audible = spokenPcmRange(pcm.samples, rate)
                        for (frame in audible) {
                            val sample = pcm.samples[frame]
                            if (!buffer.hasRemaining()) {
                                wav.write(buffer.array(), 0, buffer.position())
                                buffer.clear()
                                ensureActive()
                            }
                            buffer.putShort((amplifyNarration(sample, gain) * 32767f).toInt().toShort())
                        }
                        if (buffer.position() > 0) wav.write(buffer.array(), 0, buffer.position())
                        val pauseMs = if (index == ranges.lastIndex) pauseAfterMs else clausePause(raw)
                        val silenceBytes = rate * pauseMs / 1000 * 2
                        var pending = silenceBytes
                        if (pending > 0) buffer.array().fill(0)
                        while (pending > 0) {
                            val length = minOf(pending, buffer.capacity())
                            wav.write(buffer.array(), 0, length)
                            pending -= length
                        }
                        bytes += audible.count() * 2L + silenceBytes
                        check(bytes < Int.MAX_VALUE - 44L)
                    }
                    check(bytes > 0)
                    val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
                    header.put("RIFF".toByteArray()).putInt(36 + bytes.toInt()).put("WAVEfmt ".toByteArray())
                        .putInt(16).putShort(1.toShort()).putShort(1.toShort()).putInt(rate).putInt(rate * 2)
                        .putShort(2.toShort()).putShort(16.toShort()).put("data".toByteArray()).putInt(bytes.toInt())
                    wav.seek(0)
                    wav.write(header.array())
                }
                SynthesizedAudio(cues, bytes * 500L / rate).also {
                    ensureActive()
                    if (!compressCache || it.durationMs !in 2_000L..30_000L ||
                        !saveOpus(context, cache, key(steps), text, target, rate, it) { ensureActive() }) {
                        cache.save(text, key(steps), target, it)
                    }
                }
            } finally {
                releaseWhenIdle()
            }
        }
    private fun saveOpus(context: Context, cache: NarrationAudioCache, key: String, text: String,
        wav: File, rate: Int, audio: SynthesizedAudio, checkActive: () -> Unit): Boolean {
        if (android.os.Build.VERSION.SDK_INT < 29 ||
            android.os.SystemClock.elapsedRealtime() < retryOpusAfter) return false
        var temporary: File? = null
        return try {
            val opus = File.createTempFile("narration-", ".ogg", context.cacheDir)
            temporary = opus
            encodeNarrationOpus(wav, opus, rate, checkActive)
            checkActive()
            check(opus.length() in 45 until wav.length())
            cache.save(text, key, opus, audio.copy(opus = true))
            true
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            retryOpusAfter = android.os.SystemClock.elapsedRealtime() + 600_000L
            android.util.Log.w("TuturiumVoice", "Cache Opus sementara memakai PCM", error)
            false
        } finally {
            temporary?.delete()
        }
    }

}

internal fun synthesisThreadCount(available: Int): Int = when {
    available <= 1 -> 1
    available <= 3 -> 2
    else -> 3
}

internal data class SynthesizedAudio(val cues: List<AudioCue>, val durationMs: Long, val cacheHit: Boolean = false, val opus: Boolean = false)

internal data class AudioCue(val startMs: Long, val range: IntRange)

internal fun synthesisRanges(text: String, limit: Int = 220): List<IntRange> {
    require(limit >= 2)
    return buildList {
        var start = 0
        while (start < text.length) {
            if (text[start].isWhitespace()) {
                start++
                continue
            }
            var end = minOf(start + limit, text.length)
            if (end < text.length) {
                val middle = start + limit * 3 / 5
                val clause = (middle until end).lastOrNull { i ->
                    text[i] in ",;:!?—" && text[i + 1].isWhitespace()
                }
                val space = (end downTo start + 1).firstOrNull { text[it].isWhitespace() }
                end = clause?.plus(1) ?: space ?: end
                if (text[end].isLowSurrogate() && text[end - 1].isHighSurrogate()) end--
            }
            var trimmed = end
            while (trimmed > start && text[trimmed - 1].isWhitespace()) trimmed--
            add(start until trimmed)
            start = end
        }
    }
}
