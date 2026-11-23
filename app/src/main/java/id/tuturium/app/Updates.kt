package id.tuturium.app

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File
import java.net.URI
import java.net.URL
import java.security.MessageDigest
import javax.net.ssl.HttpsURLConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class Release(val version: String, val notes: String, val date: String, val size: Long,
    val url: String, val checksum: String)

internal class UpdateCheckException(message: String) : java.io.IOException(message)

object Updates {
    private const val REPO = "WynnDev-rill/Tuturium"
    private const val APK = "Tuturium.apk"
    private const val MAX_APK = 150L * 1024 * 1024

    suspend fun latest(): Release? = withContext(Dispatchers.IO) {
        val connection = request("https://api.github.com/repos/$REPO/releases/latest")
        try {
            when (val status = connection.responseCode) {
                200 -> Unit
                404 -> throw UpdateCheckException("GitHub belum memiliki rilis publik Tuturium.")
                403, 429 -> throw UpdateCheckException("GitHub membatasi pemeriksaan sementara. Coba lagi nanti.")
                else -> throw UpdateCheckException("GitHub tidak dapat memeriksa pembaruan (HTTP $status).")
            }
            val response = connection.inputStream.bufferedReader().use { reader ->
                val buffer = CharArray(8192)
                val text = StringBuilder()
                while (true) {
                    val length = reader.read(buffer)
                    if (length < 0) break
                    check(text.length + length <= 1_000_000)
                    text.append(buffer, 0, length)
                }
                JSONObject(text.toString())
            }
            parseLatestRelease(response, BuildConfig.VERSION_NAME)
        } finally {
            connection.disconnect()
        }
    }

    internal fun parseLatestRelease(response: JSONObject, installedVersion: String): Release? {
        val tag = response.getString("tag_name")
        if (!isNewer(tag, installedVersion) || response.optBoolean("prerelease") ||
            response.optBoolean("draft")) return null
        val assets = response.optJSONArray("assets")
            ?: throw UpdateCheckException("Rilis tersedia, tetapi APK belum siap.")
        val asset = (0 until assets.length()).map(assets::getJSONObject)
            .firstOrNull { it.optString("name") == APK && it.optString("state") == "uploaded" }
            ?: throw UpdateCheckException("Rilis tersedia, tetapi APK belum siap.")
        val url = asset.getString("browser_download_url")
        val checksum = asset.optString("digest")
        val size = asset.getLong("size")
        if (url != "https://github.com/$REPO/releases/download/$tag/$APK" || size !in 1..MAX_APK ||
            !Regex("^sha256:[a-f0-9]{64}$").matches(checksum)) {
            throw UpdateCheckException("Rilis tersedia, tetapi metadata APK belum dapat diverifikasi.")
        }
        return Release(tag.removePrefix("v"), response.optString("body").trim().take(6000),
            response.optString("published_at").take(10), size, url, checksum.removePrefix("sha256:"))
    }


    suspend fun download(context: Context, release: Release, onProgress: (Float) -> Unit): File =
        withContext(Dispatchers.IO) {
            val folder = File(context.cacheDir, "updates").apply { mkdirs() }
            val partial = File.createTempFile("download-", ".apk", folder)
            try {
                val connection = downloadConnection(release.url)
                val hash = MessageDigest.getInstance("SHA-256")
                try {
                    connection.inputStream.use { input ->
                        partial.outputStream().use { output ->
                            val buffer = ByteArray(65536)
                            var size = 0L
                            var percent = -1
                            while (true) {
                                ensureActive()
                                val length = input.read(buffer)
                                if (length < 0) break
                                size += length
                                check(size <= release.size && size <= MAX_APK) { "Ukuran APK tidak sesuai." }
                                hash.update(buffer, 0, length)
                                output.write(buffer, 0, length)
                                val next = (size * 100 / release.size).toInt()
                                if (next != percent) {
                                    percent = next
                                    onProgress(next / 100f)
                                }
                            }
                            check(size == release.size) { "Unduhan APK belum lengkap." }
                        }
                    }
                } finally {
                    connection.disconnect()
                }
                check(hash.digest().joinToString("") { "%02x".format(it) } == release.checksum) { "SHA-256 APK tidak cocok." }
                verifyPackage(context, partial, release.version)
                val target = File(folder, "Tuturium-${release.version}.apk")
                check(partial.renameTo(target)) { "Gagal menyimpan APK." }
                folder.listFiles()?.filter { it != target && it.extension == "apk" }?.forEach { it.delete() }
                target
            } finally {
                partial.delete()
            }
        }

    fun install(context: Context, file: File): Boolean {
        if (!context.packageManager.canRequestPackageInstalls()) {
            context.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}")))
            return false
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.updates", file)
        context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
        return true
    }

    @Suppress("DEPRECATION")
    private fun verifyPackage(context: Context, file: File, version: String) {
        val pm = context.packageManager
        val flags = if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
        val installed = pm.getPackageInfo(context.packageName, flags)
        val archive = pm.getPackageArchiveInfo(file.path, flags) ?: error("APK tidak dapat diverifikasi.")
        check(archive.packageName == installed.packageName && archive.versionName == version &&
            code(archive) > code(installed)) { "Paket atau versi APK tidak sesuai." }
        fun signers(info: PackageInfo) = if (Build.VERSION.SDK_INT >= 28)
            info.signingInfo?.apkContentsSigners.orEmpty().toSet() else info.signatures.orEmpty().toSet()
        val existing = signers(installed)
        check(existing.isNotEmpty() && signers(archive) == existing) { "Sertifikat APK tidak cocok." }
    }

    @Suppress("DEPRECATION")
    private fun code(info: PackageInfo) = if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else info.versionCode.toLong()

    private fun request(url: String): HttpsURLConnection = (URL(url).openConnection() as HttpsURLConnection).apply {
        instanceFollowRedirects = false
        connectTimeout = 10_000
        readTimeout = 25_000
        setRequestProperty("User-Agent", "Tuturium-Android/${BuildConfig.VERSION_NAME}")
        setRequestProperty("Accept", "application/vnd.github+json")
    }

    private fun downloadConnection(url: String): HttpsURLConnection {
        var target = url
        repeat(5) {
            val parsed = URI(target)
            check(parsed.scheme == "https" && parsed.userInfo == null && parsed.port == -1 &&
                parsed.host in setOf("github.com", "release-assets.githubusercontent.com", "objects.githubusercontent.com"))
            val connection = request(target)
            var keep = false
            try {
                when (connection.responseCode) {
                    200 -> { keep = true; return connection }
                    301, 302, 303, 307, 308 -> target =
                        parsed.resolve(connection.getHeaderField("Location") ?: error("Tautan unduhan tidak lengkap.")).toString()
                    else -> error("Unduhan gagal (HTTP ${connection.responseCode}).")
                }
            } finally {
                if (!keep) connection.disconnect()
            }
        }
        error("Pengalihan unduhan terlalu banyak.")
    }
}
