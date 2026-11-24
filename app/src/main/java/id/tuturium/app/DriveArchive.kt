package id.tuturium.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** Versioned, per-account archive stored inside Google Drive's hidden appDataFolder.
 * No service credentials or OAuth access tokens are written into this archive. */
internal class DriveArchive(private val context: Context) {
    private val library = Library.get(context)
    private val preferences = listOf(
        "settings", "listening_options", "listening_stats", "library_actions", "library_folders"
    )
    private val validName = Regex("""books/[0-9a-fA-F-]{36}\.(json|pdf)""")

    fun export(destination: File) {
        val active = library.books()
        val deleted = library.trashed()
        val index = JSONObject().put("schema", 1)
            .put("written", System.currentTimeMillis())
            .put("books", JSONArray())
            .put("preferences", JSONObject())
        val items = index.getJSONArray("books")
        (active + deleted).distinctBy { it.id }.forEach { book ->
            items.put(JSONObject()
                .put("id", book.id)
                .put("title", book.title)
                .put("format", book.format)
                .put("count", book.count)
                .put("position", book.position)
                .put("wordOffset", book.wordOffset)
                .put("updated", book.updated)
                .put("created", book.created)
                .put("words", book.words)
                .put("deleted", book.id in deleted.map { it.id }.toSet())
                .put("marks", JSONArray().apply {
                    library.bookmarks(book.id).forEach { mark ->
                        put(JSONObject().put("position", mark.index).put("excerpt", mark.excerpt))
                    }
                }))
        }
        val prefObject = index.getJSONObject("preferences")
        preferences.forEach { file ->
            val values = JSONObject()
            context.getSharedPreferences(file, Context.MODE_PRIVATE).all.forEach { (key, value) ->
                val tag = when (value) {
                    is Boolean -> "boolean"
                    is Int -> "integer"
                    is Long -> "long"
                    is Float -> "float"
                    is String -> "string"
                    is Set<*> -> "stringSet"
                    else -> null
                }
                if (tag != null) {
                    values.put(key, JSONObject().put("type", tag).put("value",
                        if (value is Set<*>) JSONArray(value.toList()) else value))
                }
            }
            prefObject.put(file, values)
        }
        destination.parentFile?.mkdirs()
        ZipOutputStream(destination.outputStream().buffered()).use { out ->
            fun addEntry(path: String, contents: ByteArray) {
                out.putNextEntry(ZipEntry(path))
                out.write(contents)
                out.closeEntry()
            }
            addEntry("manifest.json", index.toString().toByteArray(Charsets.UTF_8))
            (active + deleted).distinctBy { it.id }.forEach { book ->
                val sections = JSONArray()
                library.document(book.id).sections.forEach { section ->
                    sections.put(JSONObject().put("title", section.title).put("text", section.text))
                }
                addEntry("books/${book.id}.json", sections.toString().toByteArray(Charsets.UTF_8))
                library.originalPdf(book.id)?.let { pdf ->
                    out.putNextEntry(ZipEntry("books/${book.id}.pdf"))
                    pdf.inputStream().buffered().use { it.copyTo(out, 65536) }
                    out.closeEntry()
                }
            }
        }
    }

    /** Restores only newer cloud records; existing books never disappear on failure.
     * All untrusted entries are staged within cacheDir and validated before import. */
    fun restore(archive: File): Int {
        require(archive.isFile && archive.length() <= MAX_ARCHIVE_BYTES) {
            "Arsip Drive tidak ditemukan atau terlalu besar."
        }
        val temporary = File(context.cacheDir, "drive-restore-${UUID.randomUUID()}")
        require(temporary.mkdir())
        try {
            var entries = 0
            var uncompressed = 0L
            ZipInputStream(archive.inputStream().buffered()).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    require(entry.name == "manifest.json" || validName.matches(entry.name)) {
                        "Nama berkas arsip tidak dikenali."
                    }
                    require(!entry.isDirectory) { "Direktori tidak diizinkan." }
                    require(++entries <= 10_002) { "Terlalu banyak berkas." }
                    val dst = File(temporary, entry.name.substringAfterLast('/'))
                    require(!dst.exists()) { "Berkas arsip duplikat." }
                    dst.outputStream().buffered().use { output ->
                        val buffer = ByteArray(65536)
                        while (true) {
                            val n = zip.read(buffer)
                            if (n < 0) break
                            uncompressed += n
                            require(uncompressed <= MAX_ARCHIVE_BYTES) { "Arsip terlalu besar." }
                            output.write(buffer, 0, n)
                        }
                    }
                    zip.closeEntry()
                }
            }
            val manifestFile = File(temporary, "manifest.json")
            require(manifestFile.isFile) { "Manifest arsip tidak ditemukan." }
            val root = JSONObject(manifestFile.readText())
            require(root.getInt("schema") == 1) { "Versi arsip tidak didukung." }
            val records = root.getJSONArray("books")
            var restored = 0
            for (i in 0 until records.length()) {
                val value = records.getJSONObject(i)
                val id = UUID.fromString(value.getString("id")).toString()
                val content = File(temporary, "$id.json")
                require(content.isFile) { "Teks buku tidak lengkap di Google Drive." }
                val book = Book(
                    id = id, title = value.getString("title"),
                    format = value.getString("format"),
                    count = value.getInt("count"),
                    position = value.getInt("position"),
                    updated = value.getLong("updated"),
                    words = value.optInt("words", 1),
                    created = value.optLong("created", value.getLong("updated")),
                    wordOffset = value.optInt("wordOffset", 0).coerceAtLeast(0)
                )
                val marks = value.optJSONArray("marks") ?: JSONArray()
                val bookmarks = List(marks.length()) { n ->
                    val item = marks.getJSONObject(n)
                    Bookmark(item.getInt("position"), item.getString("excerpt"))
                }
                val pdf = File(temporary, "$id.pdf").takeIf { it.isFile }
                if (library.restoreCloudBook(
                        book, value.optBoolean("deleted"), content.readText(), bookmarks, pdf
                    )) restored++
            }
            root.optJSONObject("preferences")?.let(::restorePreferences)
            return restored
        } finally {
            temporary.deleteRecursively()
        }
    }

    private fun restorePreferences(saved: JSONObject) {
        preferences.forEach { file ->
            val source = saved.optJSONObject(file) ?: return@forEach
            val prefs = context.getSharedPreferences(file, Context.MODE_PRIVATE)
            // A newer local preference must never be overwritten by a stale cloud archive.
            if (prefs.all.isNotEmpty()) return@forEach
            val editor = prefs.edit()
            val keys = source.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val item = source.optJSONObject(key) ?: continue
                when (item.optString("type")) {
                    "boolean" -> editor.putBoolean(key, item.getBoolean("value"))
                    "integer" -> editor.putInt(key, item.getInt("value"))
                    "long" -> editor.putLong(key, item.getLong("value"))
                    "float" -> editor.putFloat(key, item.getDouble("value").toFloat())
                    "string" -> editor.putString(key, item.getString("value"))
                    "stringSet" -> {
                        val strings = item.getJSONArray("value")
                        editor.putStringSet(key, (0 until strings.length())
                            .map(strings::getString).toSet())
                    }
                }
            }
            editor.commit()
        }
    }

    companion object {
        internal const val MAX_ARCHIVE_BYTES = 1024L * 1024L * 1024L
    }
}
