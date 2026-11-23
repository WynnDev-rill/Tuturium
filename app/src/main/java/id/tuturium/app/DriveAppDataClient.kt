package id.tuturium.app

import android.util.Log
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.io.RandomAccessFile

/** Small Drive v3 REST client. Uses only drive.appdata and never persists tokens.
 * All methods block and must run on Dispatchers.IO, not the UI thread. */
internal class DriveAppDataClient(private val token: String) {
    init { require(token.isNotBlank()) }

    data class RemoteFile(val id: String, val md5: String?)

    fun findArchive(): RemoteFile? {
        val query = URLEncoder.encode("name = '$FILE_NAME' and trashed = false", "UTF-8")
        val url = "$API/files?spaces=appDataFolder&q=$query&fields=nextPageToken,files(id,name,md5Checksum)&pageSize=100"
        val response = json(url)
        val files = response.optJSONArray("files") ?: return null
        for (i in 0 until files.length()) {
            val file = files.getJSONObject(i)
            if (file.optString("name") == FILE_NAME) return RemoteFile(
                file.getString("id"), file.optString("md5Checksum").ifBlank { null })
        }
        return null
    }

    fun download(remote: RemoteFile, destination: File) {
        val conn = connection("$API/files/${remote.id}?alt=media", "GET")
        try {
            checkResponse(conn)
            destination.outputStream().buffered().use { out ->
                conn.inputStream.use { it.copyTo(out, 65536) }
            }
            if (remote.md5 != null && remote.md5 != checksum(destination))
                throw IOException("Checksum arsip Google Drive tidak cocok.")
        } finally {
            conn.disconnect()
        }
    }

    fun upload(source: File, existing: RemoteFile?): RemoteFile {
        require(source.isFile && source.length() > 0)
        val endpoint = if (existing == null)
            "$UPLOAD/files?uploadType=resumable&fields=id,md5Checksum"
        else "$UPLOAD/files/${existing.id}?uploadType=resumable&fields=id,md5Checksum"
        val init = connection(endpoint, "POST").apply {
            if (existing != null) setRequestProperty("X-HTTP-Method-Override", "PATCH")
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("X-Upload-Content-Type", "application/zip")
            setRequestProperty("X-Upload-Content-Length", source.length().toString())
            doOutput = true
        }
        val metadata = if (existing == null)
            JSONObject().put("name", FILE_NAME).put("mimeType", "application/zip")
                .put("parents", org.json.JSONArray().put("appDataFolder"))
        else JSONObject().put("name", FILE_NAME)
        val sessionUrl: String
        try {
            val bytes = metadata.toString().toByteArray(Charsets.UTF_8)
            init.setFixedLengthStreamingMode(bytes.size)
            init.outputStream.use { it.write(bytes) }
            checkResponse(init)
            sessionUrl = init.getHeaderField("Location")
                ?: throw IOException("Google Drive tidak mengembalikan sesi unggah.")
            require(sessionUrl.startsWith("https://www.googleapis.com/")) {
                "URL sesi unggahan tidak valid."
            }
        } finally {
            init.disconnect()
        }
        val length = source.length()
        var offset = 0L
        RandomAccessFile(source, "r").use { file ->
            while (offset < length) {
                val size = minOf(CHUNK_SIZE.toLong(), length - offset).toInt()
                val end = offset + size - 1
                val request = connection(sessionUrl, "PUT").apply {
                    setRequestProperty("Content-Type", "application/zip")
                    setRequestProperty("Content-Range", "bytes $offset-$end/$length")
                    setFixedLengthStreamingMode(size)
                    doOutput = true
                }
                try {
                    request.outputStream.use { output ->
                        file.seek(offset)
                        val buffer = ByteArray(65536)
                        var remaining = size
                        while (remaining > 0) {
                            val n = file.read(buffer, 0, minOf(buffer.size, remaining))
                            if (n <= 0) throw IOException("Arsip lokal terpotong.")
                            output.write(buffer, 0, n)
                            remaining -= n
                        }
                    }
                    val status = request.responseCode
                    if (status == 308) {
                        val range = request.getHeaderField("Range")
                        val confirmed = range?.substringAfterLast('-')?.toLongOrNull()
                        offset = if (confirmed != null) confirmed + 1 else end + 1
                    } else {
                        checkResponse(request)
                        val completed = JSONObject(request.inputStream.bufferedReader().use { it.readText() })
                        val id = completed.optString("id").ifBlank { existing?.id }
                            ?: throw IOException("Google Drive tidak mengembalikan file ID.")
                        val md5 = completed.optString("md5Checksum").ifBlank { null }
                        if (md5 != null && md5 != checksum(source))
                            throw IOException("Unggahan Drive gagal diverifikasi.")
                        return RemoteFile(id, md5)
                    }
                } finally {
                    request.disconnect()
                }
            }
        }
        throw IOException("Google Drive tidak menyelesaikan unggahan.")
    }

    private fun json(url: String): JSONObject {
        val conn = connection(url, "GET")
        return try {
            checkResponse(conn)
            JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
        } finally { conn.disconnect() }
    }

    private fun connection(url: String, method: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 25_000
            readTimeout = 90_000
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/json")
            useCaches = false
        }

    private fun checkResponse(conn: HttpURLConnection) {
        val status = conn.responseCode
        if (status !in 200..299) {
            // Never log OAuth tokens or raw HTTP response bodies.
            Log.w("TuturiumCloud", "Drive API returned HTTP $status")
            throw IOException(when (status) {
                401 -> "Akses Google Drive kedaluwarsa. Hubungkan kembali akun."
                403 -> "Izin Google Drive ditolak. Izinkan penyimpanan aplikasi."
                429, 500, 502, 503, 504 -> "Google Drive sedang sibuk. Coba lagi."
                else -> "Google Drive gagal (HTTP $status)."
            })
        }
    }

    companion object {
        const val FILE_NAME = "tuturium-cloud-v1.zip"
        private const val API = "https://www.googleapis.com/drive/v3"
        private const val UPLOAD = "https://www.googleapis.com/upload/drive/v3"
        private const val CHUNK_SIZE = 8 * 1024 * 1024

        fun checksum(file: File): String {
            val digest = MessageDigest.getInstance("MD5")
            file.inputStream().buffered().use { input ->
                val block = ByteArray(65536)
                while (true) {
                    val n = input.read(block)
                    if (n < 0) break
                    digest.update(block, 0, n)
                }
            }
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}
