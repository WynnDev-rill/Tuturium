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
            numThreads = 3))).also { sherpa = it }
    }

    private val lifetime = CoroutineScope(SupervisorJob() + dispatcher)
    private var releaseJob: Job? = null

    suspend fun write(context: Context, voice: OfflineVoice, text: String, target: File): SynthesizedAudio =
        withContext(dispatcher) {
            releaseJob?.cancel()
            try {
                var rate = 0
                var bytes = 0L
                val cues = mutableListOf<AudioCue>()
                val buffer = ByteBuffer.allocate(16384).order(ByteOrder.LITTLE_ENDIAN)
                java.io.RandomAccessFile(target, "rw").use { wav ->
                    wav.setLength(0)
                    wav.write(ByteArray(44))
                    val engine = load(context)
                    check(voice.speaker in 0 until engine.numSpeakers())
                    for (range in synthesisRanges(text)) {
                        ensureActive()
                        // Synthesize at the model's baseline. The player controls listening speed.
                        val pcm = engine.generateWithConfig(text.substring(range), GenerationConfig(
                            sid = voice.speaker, speed = 1f, numSteps = 8, extra = mapOf("lang" to "id")))
                        ensureActive()
                        check(pcm.samples.isNotEmpty() && pcm.sampleRate > 0)
                        if (rate == 0) rate = pcm.sampleRate
                        check(rate == pcm.sampleRate)
                        cues += AudioCue(bytes * 500L / rate, range)
                        buffer.clear()
                        for (sample in pcm.samples) {
                            if (!buffer.hasRemaining()) {
                                wav.write(buffer.array(), 0, buffer.position())
                                buffer.clear()
                                ensureActive()
                            }
                            buffer.putShort((sample.coerceIn(-1f, 1f) * 32767f).toInt().toShort())
                        }
                        if (buffer.position() > 0) wav.write(buffer.array(), 0, buffer.position())
                        bytes += pcm.samples.size * 2L
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
                SynthesizedAudio(cues, bytes * 500L / rate)
            } finally {
                releaseJob = lifetime.launch {
                    delay(45_000)
                    runCatching { sherpa?.release() }
                    sherpa = null
                }
            }
        }
}

internal data class SynthesizedAudio(val cues: List<AudioCue>, val durationMs: Long)

internal data class AudioCue(val startMs: Long, val range: IntRange)

internal fun synthesisRanges(text: String, limit: Int = 180): List<IntRange> {
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
                val space = text.lastIndexOf(' ', end)
                if (space > start) end = space
                if (text[end].isLowSurrogate() && text[end - 1].isHighSurrogate()) end--
            }
            var trimmed = end
            while (trimmed > start && text[trimmed - 1].isWhitespace()) trimmed--
            add(start until trimmed)
            start = end
        }
    }
}
