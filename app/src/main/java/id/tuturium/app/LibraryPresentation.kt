package id.tuturium.app

import android.content.Context
import kotlin.math.ceil

/** UI estimates, not a stopwatch. Actual duration depends on the installed TTS voice. */
internal fun estimateRemainingMinutes(wordCount: Int, passCount: Int, position: Int, speed: Float): Int {
    if (wordCount <= 0 || passCount <= 0) return 0
    val ratio = (passCount - position.coerceIn(0, passCount)).toDouble() / passCount
    val wpm = 150.0 * speed.coerceIn(0.5f, 3f)
    return ceil(wordCount * ratio / wpm).toInt()
}

internal fun countDocumentWords(text: String): Int =
    Regex("""[\p{L}\p{N}]+(?:['’-][\p{L}\p{N}]+)*""").findAll(text).count()

internal fun sortBooks(books: List<Book>, sort: String): List<Book> =
    when(sort) {
        "Tanggal ditambahkan" -> books.sortedWith(compareByDescending<Book> { it.created }.thenBy { it.title })
        "Abjad" -> books.sortedBy { it.title.lowercase() }
        else -> books.sortedWith(compareByDescending<Book> { it.updated }.thenBy { it.title })
    }

/** Local-only pin, read later, and listening queue. No subscription or cloud. */
internal object BookActions {
    private const val FILE = "library_actions"
    private fun prefs(c: Context) = c.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    private fun ids(c: Context, key: String): Set<String> =
        prefs(c).getStringSet(key, emptySet()).orEmpty().toSet()

    fun pinned(c: Context, id: String) = id in ids(c, "pins")
    fun later(c: Context, id: String) = id in ids(c, "later")
    fun togglePin(c: Context, id: String) = toggle(c, "pins", id)
    fun toggleLater(c: Context, id: String) = toggle(c, "later", id)

    private fun toggle(c: Context, key: String, id: String) {
        val values = ids(c, key).toMutableSet()
        if (!values.add(id)) values.remove(id)
        prefs(c).edit().putStringSet(key, values).apply()
        TuturiumCloud.markDirty(c)
    }

    fun enqueue(c: Context, id: String, next: Boolean = false) {
        val values = queue(c).filterNot { it == id }.toMutableList()
        if (next) values.add(0, id) else values.add(id)
        prefs(c).edit().putString("queue", values.joinToString(",")).apply()
        TuturiumCloud.markDirty(c)
    }

    fun queue(c: Context): List<String> =
        prefs(c).getString("queue", "").orEmpty().split(",").filter { it.isNotBlank() }

    fun dequeue(c: Context): String? {
        val values = queue(c).toMutableList()
        if (values.isEmpty()) return null
        val first = values.removeAt(0)
        prefs(c).edit().putString("queue", values.joinToString(",")).apply()
        TuturiumCloud.markDirty(c)
        return first
    }

    fun removeFromQueue(c: Context, id: String) {
        prefs(c).edit().putString("queue",
            queue(c).filterNot { it == id }.joinToString(",")).apply()
        TuturiumCloud.markDirty(c)
    }

    fun remove(c: Context, id: String) {
        for (key in listOf("pins", "later")) {
            prefs(c).edit().putStringSet(key, ids(c, key) - id).apply()
        }
        prefs(c).edit().putString("queue", queue(c).filterNot { it == id }.joinToString(",")).apply()
        TuturiumCloud.markDirty(c)
    }
}

internal fun matchesLibraryFilter(book: Book, filter: String, later: Boolean): Boolean =
    when (filter) {
        "Belum dibaca" -> book.position == 0 && book.wordOffset == 0
        "Baca nanti" -> later
        "PDF" -> book.format == "PDF"
        "Dokumen" -> book.format in setOf("TXT", "DOCX", "EPUB", "DOC", "RTF", "MD")
        "Halaman web" -> book.format == "WEB"
        "Pindai" -> book.format in setOf("OCR", "SCAN", "IMAGE", "JPG", "JPEG", "PNG")
        "Lainnya" -> book.format !in setOf("PDF", "TXT", "DOCX", "EPUB", "DOC", "RTF", "MD", "WEB", "OCR", "SCAN", "IMAGE", "JPG", "JPEG", "PNG")
        else -> true
    }
