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
    private fun file(name: String, base: String, sha: String, size: Long) = ModelFile(name, base + name, sha, size)
    private fun tar(id: String, label: String, name: String, sha: String, size: Long,
        labels: List<String>, required: List<String>) =
        OfflineModel(id, label, listOf(file(name, SHERPA, sha, size)), labels, required, true)

    val models = listOf(
        tar("supertonic3", "Supertonic 3 · INT8", "sherpa-onnx-supertonic-3-tts-int8-2026-05-11.tar.bz2",
            "82fa96f91c4ef8abaae3a14a3f4153facf88bed821d1f7331cec2700f432c427", 128774318,
            (1..5).map { "Supertonic F$it" } + (1..5).map { "Supertonic M$it" },
            listOf("duration_predictor.int8.onnx", "text_encoder.int8.onnx", "vector_estimator.int8.onnx",
                "vocoder.int8.onnx", "tts.json", "unicode_indexer.bin", "voice.bin"))
    )
    // Only the repeated denoising stage changes. All other installed files are byte-identical.
    val acceleratedVector = ModelFile(
        "vector_estimator.mobile.int8.onnx",
        "https://huggingface.co/WilburDev/supertonic-3-mobile/resolve/" +
            "2d53d752a6e9a1db1bd0f63fe36806dc6d1a3ee5/vector_estimator.int8.onnx",
        "6fd0f1637a0192ec2ec160a899045d8138395100a3c25414134adf76abd3f57b",
        65_967_881L
    )
    private val voicesById = models.flatMap { it.voices }.associateBy { it.id }
    private val modelsById = models.associateBy { it.id }
    fun voice(id: String?): OfflineVoice? = voicesById[id]
    fun supportedSelection(id: String?): String? =
        id?.takeUnless { it.startsWith("offline:") && voice(it) == null }
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

    fun acceleratedVector(context: Context, model: OfflineModel): File? =
        File(directory(context, model), OfflineCatalog.acceleratedVector.name).takeIf {
            it.isFile && it.length() == OfflineCatalog.acceleratedVector.size
        }

    suspend fun installAcceleration(context: Context, model: OfflineModel, progress: (Float?) -> Unit) =
        withContext(Dispatchers.IO) {
            if (acceleratedVector(context, model) != null) return@withContext
            check(installed(context, model)) { "Unduh model Supertonic terlebih dahulu." }
            val directory = directory(context, model)
            val spec = OfflineCatalog.acceleratedVector
            val temporary = File.createTempFile("acceleration-", ".partial", directory)
            try {
                var done = 0L
                var lastPercent = -1
                downloadVerified(spec, temporary) { count ->
                    done += count
                    val percent = (done * 100 / spec.size).toInt()
                    if (percent != lastPercent) {
                        lastPercent = percent
                        progress(percent / 100f)
                    }
                }
                ensureActive()
                check(temporary.renameTo(File(directory, spec.name))) { "Gagal memasang akselerasi." }
            } finally { temporary.delete() }
        }

    private suspend fun downloadVerified(spec: ModelFile, target: File, onBytes: (Int) -> Unit) {
        val job = kotlinx.coroutines.currentCoroutineContext()
        val connection = (URL(spec.url).openConnection() as HttpsURLConnection).apply {
            connectTimeout = 30_000
            readTimeout = 30_000
            instanceFollowRedirects = true
        }
        try {
            check(connection.responseCode == 200) { "Server model: HTTP " + connection.responseCode }
            val digest = MessageDigest.getInstance("SHA-256")
            var count = 0L
            connection.inputStream.use { input ->
                target.outputStream().buffered().use { output ->
                    val buffer = ByteArray(65_536)
                    while (true) {
                        job.ensureActive()
                        val read = input.read(buffer)
                        if (read < 0) break
                        count += read
                        check(count <= spec.size) { "File model terlalu besar." }
                        digest.update(buffer, 0, read)
                        output.write(buffer, 0, read)
                        onBytes(read)
                    }
                }
            }
            check(count == spec.size) { "Unduhan model tidak lengkap." }
            val hash = digest.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
            check(hash == spec.sha256) { "Checksum model tidak cocok." }
        } finally { connection.disconnect() }
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
                    downloadVerified(spec, target) { count ->
                        done += count
                        val percent = (done * 100 / total).toInt().coerceIn(0, 100)
                        if (percent != lastPercent) {
                            lastPercent = percent
                            progress(percent / 100f)
                        }
                    }
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
