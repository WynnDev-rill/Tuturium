package id.tuturium.app

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import java.io.File
import java.net.URL
import java.security.MessageDigest
import javax.net.ssl.HttpsURLConnection

internal data class ModelFile(val name: String, val url: String, val sha256: String, val size: Long)
internal data class OfflineVoice(val id: String, val model: String, val label: String, val speaker: Int)
internal data class OfflineModel(val id: String, val label: String, val files: List<ModelFile>,
    val labels: List<String>, val required: List<String>, val archive: Boolean = false) {
    val voices = labels.mapIndexed { i, label -> OfflineVoice("offline:$id:$i", id, label, i) }
    val bytes get() = files.sumOf { it.size }
}

internal object OfflineCatalog {
    private const val SHERPA = "https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/"
    private const val ARBO = "https://github.com/ARBO-TEAM/arboTTS/releases/download/"
    private const val ARBO_RAW = "https://raw.githubusercontent.com/ARBO-TEAM/arboTTS/f628060f0a3afdf86019c99eb057dcecb824d294/"
    private const val KOKORO = "https://huggingface.co/onnx-community/Kokoro-82M-ONNX/resolve/f46687f7e41512228ae953af24a11b2640ea0f22/"
    private fun file(name: String, base: String, sha: String, size: Long) = ModelFile(name, base + name, sha, size)
    private fun tar(id: String, label: String, name: String, sha: String, size: Long,
        labels: List<String>, required: List<String>) =
        OfflineModel(id, label, listOf(file(name, SHERPA, sha, size)), labels, required, true)

    val models = listOf(
        tar("supertonic3", "Supertonic 3 · INT8", "sherpa-onnx-supertonic-3-tts-int8-2026-05-11.tar.bz2",
            "82fa96f91c4ef8abaae3a14a3f4153facf88bed821d1f7331cec2700f432c427", 128774318,
            (1..5).map { "Supertonic F$it" } + (1..5).map { "Supertonic M$it" },
            listOf("duration_predictor.int8.onnx", "text_encoder.int8.onnx", "vector_estimator.int8.onnx",
                "vocoder.int8.onnx", "tts.json", "unicode_indexer.bin", "voice.bin")),
        tar("piper", "Piper · News TTS", "vits-piper-id_ID-news_tts-medium.tar.bz2",
            "634b0169fe41c368e9fafe128f30647f9e0301e852ce503cd78146db524f1f12", 67243848,
            listOf("Piper News Indonesia"), listOf("id_ID-news_tts-medium.onnx", "tokens.txt", "espeak-ng-data")),
        OfflineModel("arbo-nano", "ArboTTS · Paradee Nano",
            listOf(
                file("arbo_paradee_8m.onnx", ARBO + "v2.4.0/", "0a858866e77923cc2f8f18dbcad2c398db4c99d60d39725ffd544f5f829d418c", 9037971),
                file("vocab.json", ARBO + "v2.4.0/", "2f463e209782e9a97c84c29bb7e6267a5eb9569db525e6b83d170d4a5ff73721", 1489)),
            listOf("Ardi"), listOf("arbo_paradee_8m.onnx", "vocab.json")),
        OfflineModel("arbo-small", "ArboTTS · Small",
            listOf(
                file("arbotts_small.onnx", ARBO + "v2.1.0/", "8fe4f5e9edf68f5c758ba7e4e45c817a6669ea602a1b4afe3495e1bfc1b0feb8", 32806261),
                file("arbotts_small.onnx.json", ARBO + "v2.1.0/", "747d9326512a9ffdae36fb4115b127df4391b89353b0f33759ba56ab2ad204f1", 5878)),
            listOf("Arbo Small", "Arba Small"), listOf("arbotts_small.onnx", "arbotts_small.onnx.json")),
        OfflineModel("arbo-standard", "ArboTTS · Standard",
            listOf(
                file("arbotts_v1.onnx", ARBO + "v2.0.0/", "752ead6795d369064fd957ff0aeed62d851f559289d30c0fe73bf70f2632192b", 63516051),
                file("arbotts_v1.onnx.json", ARBO + "v2.0.0/", "cadd94c06f27a201dc5174f07a5f4887a24f88b5f182cec055c6beba0b5ed51b", 7994)),
            listOf("Arbo Standard"), listOf("arbotts_v1.onnx", "arbotts_v1.onnx.json")),
        OfflineModel("arbo-kokoro", "ArboTTS · Kokoro",
            listOf(
                ModelFile("model_quantized.onnx", KOKORO + "onnx/model_quantized.onnx",
                    "0d55b15d4b735d61a21b0105136bc81b8768c4db94753193c19354fa863cd556", 92360543),
                file("tokenizer.json", KOKORO, "ee301fc39cf903ddbb463564630a28767785e3a11edd6d8226e92d4b4ef131bb", 4608),
                file("id_arbo.bin", ARBO_RAW + "arbotts/voices/", "83549e23f9f89f0006d5403fd1e0ceb928661a06159c80819feb93693319d895", 524288),
                file("id_arba.bin", ARBO_RAW + "arbotts/voices/", "70cb0982718e82dbca56bd44fe79bb23d9b936ae4c42c3c7691d8f417dadcd27", 524288)),
            listOf("Arbo Kokoro", "Arba Kokoro"),
            listOf("model_quantized.onnx", "tokenizer.json", "id_arbo.bin", "id_arba.bin"))
    )
    private val voicesById = models.flatMap { it.voices }.associateBy { it.id }
    private val modelsById = models.associateBy { it.id }
    fun voice(id: String?): OfflineVoice? = voicesById[id]
    fun model(id: String): OfflineModel = modelsById.getValue(id)
}

internal object OfflineModelStore {
    private fun root(context: Context) = File(context.noBackupFilesDir, "offline-tts").apply { mkdirs() }
    fun directory(context: Context, model: OfflineModel) = File(root(context), model.id)
    fun installed(context: Context, model: OfflineModel): Boolean {
        val dir = directory(context, model)
        return File(dir, ".ready").isFile && model.required.all {
            val f = File(dir, it)
            f.isDirectory || (f.isFile && f.length() > 0)
        }
    }

    suspend fun install(context: Context, model: OfflineModel, progress: (Float?) -> Unit) =
        withContext(Dispatchers.IO) {
            if (installed(context, model)) return@withContext
            val dir = directory(context, model)
            val temporary = File(root(context), model.id + ".partial")
            temporary.deleteRecursively()
            check(temporary.mkdirs()) { "Tidak dapat membuat folder model." }
            try {
                var done = 0L
                val total = model.bytes
                var lastPercent = -1
                model.files.forEach { spec ->
                    ensureActive()
                    val target = File(temporary, spec.name)
                    val connection = (URL(spec.url).openConnection() as HttpsURLConnection).apply {
                        connectTimeout = 30_000
                        readTimeout = 30_000
                        instanceFollowRedirects = true
                    }
                    try {
                        check(connection.responseCode == 200) { "Server model ${model.id}/${spec.name}: HTTP " + connection.responseCode }
                        val digest = MessageDigest.getInstance("SHA-256")
                        connection.inputStream.use { input ->
                            target.outputStream().use { output ->
                                val buffer = ByteArray(65536)
                                var count = 0L
                                while (true) {
                                    ensureActive()
                                    val n = input.read(buffer)
                                    if (n < 0) break
                                    count += n
                                    check(count <= 400L * 1024 * 1024) { "File model terlalu besar." }
                                    digest.update(buffer, 0, n)
                                    output.write(buffer, 0, n)
                                    done += n
                                    val percent = (done * 100 / total).toInt().coerceIn(0, 100)
                                    if (percent != lastPercent) {
                                        lastPercent = percent
                                        progress(percent / 100f)
                                    }
                                }
                                if (spec.size > 0) check(count == spec.size) { "Unduhan model tidak lengkap." }
                                check(count > 0)
                            }
                        }
                        val hash = digest.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
                        check(hash == spec.sha256) { "Checksum model tidak cocok." }
                    } finally { connection.disconnect() }
                }
                progress(null)
                if (model.archive) {
                    val archive = File(temporary, model.files.single().name)
                    val expanded = File(temporary, "expanded").apply { mkdirs() }
                    var extracted = 0L
                    TarArchiveInputStream(BZip2CompressorInputStream(archive.inputStream().buffered())).use { tar ->
                        while (true) {
                            ensureActive()
                            val entry = tar.nextEntry ?: break
                            val relative = entry.name.removePrefix("./").substringAfter('/', "")
                            if (relative.isBlank()) continue
                            check(!entry.isSymbolicLink && !entry.isLink)
                            val dest = File(expanded, relative)
                            check(dest.canonicalPath.startsWith(expanded.canonicalPath + File.separator))
                            if (entry.isDirectory) { dest.mkdirs(); continue }
                            if (!entry.isFile) continue
                            extracted += entry.size
                            check(extracted <= 600L * 1024 * 1024)
                            dest.parentFile?.mkdirs()
                            dest.outputStream().use { tar.copyTo(it) }
                        }
                    }
                    archive.delete()
                    expanded.listFiles().orEmpty().forEach { check(it.renameTo(File(temporary, it.name))) }
                    expanded.delete()
                }
                check(model.required.all { File(temporary, it).exists() }) { "Isi paket model tidak lengkap." }
                File(temporary, ".ready").writeText("1")
                if (dir.exists()) dir.deleteRecursively()
                check(temporary.renameTo(dir)) { "Gagal memasang model." }
            } finally { temporary.deleteRecursively() }
        }
}
