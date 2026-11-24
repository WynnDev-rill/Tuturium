package id.tuturium.app

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.k2fsa.sherpa.onnx.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.LongBuffer
import java.util.Locale
import kotlin.math.abs

// Adapted from the MIT-licensed ARBO-TEAM/arboTTS Indonesian G2P.
private val taling = "bebek meja sore capek sate tempe lele tante nenek kakek kolega model modern konser paket tiket presiden hotel kafe periode tema teks pesta desa daerah merah lewat pena belok tenda level elemen kue kode mode ide tape toge onde bemo".split(' ').toSet()
private val pepet = "dengan penuh beli membeli dibeli pembeli pembelian beri memberi diberi pemberian temu bertemu menemui kenal mengenal dikenal terkenal pegang memegang dipegang tekan menekan ditekan tekanan renang berenang perenang senin selasa kemarin sebentar sekarang selamat semoga selalu semua seperti sedang sendiri sembilan sepuluh sebelas seratus seribu sedikit tersebut pemerintah negeri beras kera tepi teman lezat dekat besar cepat tepat berat lebat tebal gelap terang tenang senang selat tepuk remuk belut semut leher lemah kering keluar kembali kertas kepala kelapa keluarga belum benar benci benda bengkok bentak bentang bentuk berani berapa berkas berkat bersih betul cegah cekik celaka celana cemburu cemas cengkeram cerah cerita cermat cermin derajat deras derita dermaga gedung geli gempa gemetar gerak geram gerbang gereja gertak hela hembus hening hentak henti jelajah jelas jelita jemput jemur jendela jenderal jengkal jengkel jerapah jerat jerit jeruk kelabu kelasi kelelawar keledai kelinci kelompok keluh kemari kemarau kemas kembang kembar kempa kempis kemudi kendali kendaraan kendur kental kentang kentut kenyal kenyang kepada kepal keping kepiting kepul kerak keramas keranjang kerbau kerikil keringat keripik keris keriting kerudung kerumun kesal kesan kesatria ketam ketat ketemu ketik ketika ketua ketuk ketupat lebah lebam lebar lebih lecut ledak lega legam lekat lekuk lelah lelaki leluasa lemak lemari lembab lembar lembut lempeng lencana lengah lengan lengkap lengkung lenyap lepas lereng lesu letak letup letus medan megah melati melarat menara mendarat menit mentah menteri merak merang merpati mesin meski mesra mewah neraka pedang pedas pedati peduli pegawai pekan pelan pelangi pelat pelatuk pelopor peluit peluk peluru pematang pemeo penat pendek pendekar pendeta pengantin penjara perahu perak perang perangkap perban perca percaya percik perdana perempuan pergi perih periksa perintah peristiwa perkara perkasa perlahan perlu permaisuri pernah perosot persen persis pertama pertiwi perut pesan pesantren pesat pesona petak petang petani peti petik petir petualang petunjuk rebah rebung rebus reda redam redup regan regang regu rehat rejam rekat rela relung remang remas remeh rempah remuk rencana".split(' ').toSet()
private val prefixes = listOf("ber", "bel", "be", "men", "mem", "meng", "me", "per", "pel", "pe", "ter", "te", "se", "ke")
private fun resolvePepet(word: String): String {
    if (word in taling) return word
    if (word !in pepet) {
        val prefix = prefixes.firstOrNull { word.startsWith(it) && word.length > it.length + 2 }
        if (prefix != null) return prefix.replace('e', 'ə') + resolvePepet(word.substring(prefix.length))
    }
    val chars = word.toCharArray()
    val vowels = chars.indices.filter { chars[it] in "aeiou" }
    val i = if (vowels.size >= 2) vowels[vowels.size - 2] else if (word in pepet) vowels.firstOrNull() else null
    if (i != null && chars[i] == 'e') chars[i] = 'ə'
    return String(chars)
}
internal fun arboIpa(text: String): String =
    Regex("[\\p{L}\\p{N}']+|[.,!?;:\\-—–]").findAll(text.lowercase(Locale.ROOT)).map { match ->
        var word = resolvePepet(match.value)
        if (word.endsWith("k") && word.length > 2) word = word.dropLast(1) + "ʔ"
        word = word.replace("ng", "ŋ").replace("ny", "ɲ").replace("sy", "ʃ").replace("kh", "x")
            .replace("ai", "aj").replace("au", "aw").replace("oi", "oj")
        buildString { word.forEach { append(when (it) {
            'c' -> "tʃ"; 'j' -> "dʒ"; 'y' -> "j"; 'g' -> "ɡ"; else -> it.toString()
        }) } }
    }.joinToString(" ").replace(Regex("\\s+([.,!?;:\\-—–])"), "$1")

private data class Pcm(val samples: FloatArray, val rate: Int)

internal object OfflineSynth {
    // Serialize native inference, including preview, to avoid loading two models at once.
    val dispatcher = Dispatchers.IO.limitedParallelism(1)
    private var key: String? = null
    private var sherpa: OfflineTts? = null
    private var ort: OrtSession? = null
    private var vocabulary: JSONObject? = null
    private var styles: Array<FloatArray>? = null
    private val env by lazy { OrtEnvironment.getEnvironment() }

    private fun load(context: Context, voice: OfflineVoice) {
        val model = OfflineCatalog.model(voice.model)
        check(OfflineModelStore.installed(context, model)) { "Unduh model suara terlebih dahulu." }
        if (key == model.id) return
        sherpa?.release(); sherpa = null
        ort?.close(); ort = null
        vocabulary = null; styles = null; key = null
        val dir = OfflineModelStore.directory(context, model)
        fun path(file: String) = File(dir, file).absolutePath
        when (model.id) {
            "supertonic3" -> sherpa = OfflineTts(config = OfflineTtsConfig(model = OfflineTtsModelConfig(
                supertonic = OfflineTtsSupertonicModelConfig(
                    durationPredictor = path("duration_predictor.int8.onnx"),
                    textEncoder = path("text_encoder.int8.onnx"),
                    vectorEstimator = path("vector_estimator.int8.onnx"),
                    vocoder = path("vocoder.int8.onnx"), ttsJson = path("tts.json"),
                    unicodeIndexer = path("unicode_indexer.bin"), voiceStyle = path("voice.bin")),
                numThreads = 3)))
            "piper" -> sherpa = OfflineTts(config = OfflineTtsConfig(model = OfflineTtsModelConfig(
                vits = OfflineTtsVitsModelConfig(model = path("id_ID-news_tts-medium.onnx"),
                    tokens = path("tokens.txt"), dataDir = path("espeak-ng-data")), numThreads = 3)))
            else -> {
                val filename = when (model.id) {
                    "arbo-nano" -> "arbo_paradee_8m.onnx"
                    "arbo-small" -> "arbotts_small.onnx"
                    "arbo-standard" -> "arbotts_v1.onnx"
                    else -> "model_quantized.onnx"
                }
                vocabulary = when (model.id) {
                    "arbo-nano" -> JSONObject(File(dir, "vocab.json").readText())
                    "arbo-kokoro" -> JSONObject(File(dir, "tokenizer.json").readText())
                        .getJSONObject("model").getJSONObject("vocab")
                    else -> JSONObject(File(dir, "$filename.json").readText()).getJSONObject("phoneme_id_map")
                }
                if (model.id == "arbo-kokoro") styles = arrayOf("id_arbo.bin", "id_arba.bin").map {
                    val bytes = File(dir, it).readBytes()
                    check(bytes.size >= 1024 && bytes.size % 1024 == 0)
                    val b = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer()
                    FloatArray(b.remaining()).also { values -> b.get(values) }
                }.toTypedArray()
                ort = OrtSession.SessionOptions().use { opts ->
                    opts.setIntraOpNumThreads(3)
                    opts.setInterOpNumThreads(1)
                    env.createSession(path(filename), opts)
                }
            }
        }
        key = model.id
    }

    private fun generate(context: Context, voice: OfflineVoice, text: String, speed: Float): Pcm {
        load(context, voice)
        sherpa?.let { engine ->
            check(voice.speaker < engine.numSpeakers())
            val audio = engine.generateWithConfig(text, GenerationConfig(
                sid = voice.speaker, speed = speed, numSteps = 8,
                extra = if (voice.model == "supertonic3") mapOf("lang" to "id") else null))
            return Pcm(audio.samples, audio.sampleRate)
        }
        val session = checkNotNull(ort)
        val vocab = checkNotNull(vocabulary)
        val vits = voice.model == "arbo-small" || voice.model == "arbo-standard"
        val ipa = arboIpa(text)
        val ids = if (vits) {
            val phonemes = java.text.Normalizer.normalize(ipa, java.text.Normalizer.Form.NFD)
            buildList<Long> {
                add(1); add(0)
                phonemes.forEach { char ->
                    val value = vocab.optJSONArray(char.toString())?.optInt(0, -1) ?: -1
                    if (value >= 0) { add(value.toLong()); add(0) }
                }
                add(2)
            }.toLongArray()
        } else {
            val tokens = ipa.mapNotNull { c -> vocab.optInt(c.toString(), -1).takeIf { it >= 0 } }
            (listOf(0) + tokens + 0).map { it.toLong() }.toLongArray()
        }
        check(ids.size > 2) { "Tidak ada fonem yang dapat dibaca." }
        val inputs = linkedMapOf<String, OnnxTensor>()
        fun ints(name: String, data: LongArray, shape: LongArray) {
            inputs[name] = OnnxTensor.createTensor(env, LongBuffer.wrap(data), shape)
        }
        fun floats(name: String, data: FloatArray, shape: LongArray) {
            inputs[name] = OnnxTensor.createTensor(env, FloatBuffer.wrap(data), shape)
        }
        try {
            if (vits) {
                ints("input", ids, longArrayOf(1, ids.size.toLong()))
                ints("input_lengths", longArrayOf(ids.size.toLong()), longArrayOf(1))
                floats("scales", floatArrayOf(0.667f, 1f / speed, 0.8f), longArrayOf(3))
                if ("sid" in session.inputNames) ints("sid", longArrayOf(voice.speaker.toLong()), longArrayOf(1))
                else check(voice.speaker == 0) { "Model tidak mendukung pembicara ini." }
            } else {
                ints("input_ids", ids, longArrayOf(1, ids.size.toLong()))
                floats("speed", floatArrayOf(speed), longArrayOf(1))
                if (voice.model == "arbo-kokoro") {
                    val pack = checkNotNull(styles)[voice.speaker]
                    val start = (ids.size - 2).coerceIn(0, pack.size / 256 - 1) * 256
                    floats("style", pack.copyOfRange(start, start + 256), longArrayOf(1, 256))
                }
            }
            val samples = session.run(inputs).use { result ->
                val buffer = (result[0] as OnnxTensor).floatBuffer
                FloatArray(buffer.remaining()).also { buffer.get(it) }
            }
            val max = samples.maxOfOrNull { abs(it) } ?: 0f
            check(max.isFinite() && max > 0f && samples.all { it.isFinite() }) { "Model menghasilkan audio kosong." }
            val scale = if (max > 1f) 0.95f / max else 0.95f
            samples.indices.forEach { samples[it] *= scale }
            return Pcm(samples, if (vits) 22050 else 24000)
        } finally { inputs.values.forEach { it.close() } }
    }

    private val lifetime = CoroutineScope(SupervisorJob() + dispatcher)
    private var releaseJob: Job? = null

    suspend fun write(context: Context, voice: OfflineVoice, text: String, speed: Float, target: File): List<AudioCue> =
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
                    for (range in synthesisRanges(text)) {
                        ensureActive()
                        val pcm = generate(context, voice, text.substring(range.first, range.last + 1), speed.coerceIn(0.5f, 4f))
                        ensureActive()
                        check(pcm.samples.isNotEmpty() && pcm.rate > 0)
                        if (rate == 0) rate = pcm.rate
                        check(rate == pcm.rate)
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
                cues
            } finally {
                releaseJob = lifetime.launch {
                    delay(45_000)
                    runCatching { sherpa?.release() }
                    runCatching { ort?.close() }
                    sherpa = null
                    ort = null
                    vocabulary = null
                    styles = null
                    key = null
                }
            }
        }
}

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

/** Main-thread MediaPlayer lifecycle; model inference and file generation are off-main. */
internal class OfflineAudio(private val context: Context) {
    private var generation = 0
    private var job: Job? = null
    private var player: MediaPlayer? = null
    private var cueJob: Job? = null
    private var audio: File? = null
    fun stop() {
        generation++
        job?.cancel(); job = null
        cueJob?.cancel()
        cueJob = null
        player?.release(); player = null
        audio?.delete(); audio = null
    }
    fun play(scope: CoroutineScope, voice: OfflineVoice, text: String, speed: Float,
        pitch: Float = 1f, onRange: (Int, Int) -> Unit = { _, _ -> },
        onStart: () -> Unit, onDone: () -> Unit, onError: (Throwable) -> Unit) {
        stop()
        val token = generation
        job = scope.launch {
            var file: File? = null
            try {
                file = File.createTempFile("offline-voice-", ".wav", context.cacheDir)
                val cues = OfflineSynth.write(context, voice, text, speed, file)
                ensureActive()
                if (generation != token) return@launch
                audio = file
                val engine = MediaPlayer()
                player = engine
                engine.setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
                engine.setDataSource(file.absolutePath)
                engine.setOnPreparedListener {
                    if (generation == token) runCatching {
                        it.start()
                        if (pitch != 1f) it.playbackParams = it.playbackParams.setPitch(pitch.coerceIn(0.5f, 2f))
                        onStart()
                        if (cues.isNotEmpty()) cueJob = scope.launch {
                            for ((startMs, range) in cues) {
                                while (generation == token && it.currentPosition < startMs) {
                                    delay((startMs - it.currentPosition).coerceIn(16L, 250L))
                                }
                                if (generation != token) break
                                onRange(range.first, range.last + 1)
                            }
                        }
                    }.onFailure { e -> stop(); onError(e) }
                }
                engine.setOnCompletionListener {
                    if (generation == token) { stop(); onDone() }
                }
                engine.setOnErrorListener { _, what, extra ->
                    if (generation == token) { stop(); onError(IllegalStateException("Audio $what/$extra")) }
                    true
                }
                engine.prepareAsync()
            } catch (error: CancellationException) { throw error }
            catch (error: Throwable) { if (generation == token) { stop(); onError(error) } }
            finally { if (audio != file) file?.delete() }
        }
    }
}
