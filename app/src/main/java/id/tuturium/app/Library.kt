package id.tuturium.app

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.AtomicFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class Book(
    val id: String,
    val title: String,
    val format: String,
    val count: Int,
    val position: Int,
    val updated: Long,
    val words: Int = 0,
    val created: Long = updated,
    val wordOffset: Int = 0
)

data class Bookmark(val index: Int, val excerpt: String)

class Library private constructor(context: Context) :
    SQLiteOpenHelper(context, "library.db", null, 5) {
    private val appContext = context.applicationContext
    private val folder = File(context.filesDir, "books").apply { mkdirs() }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE books (id TEXT PRIMARY KEY, title TEXT NOT NULL, format TEXT NOT NULL,
                count INTEGER NOT NULL, position INTEGER NOT NULL DEFAULT 0, updated INTEGER NOT NULL,
                deleted_at INTEGER NOT NULL DEFAULT 0, words INTEGER NOT NULL DEFAULT 0,
                created INTEGER NOT NULL DEFAULT 0, word_offset INTEGER NOT NULL DEFAULT 0)"""
        )
        db.execSQL(
            """CREATE TABLE bookmarks (book TEXT REFERENCES books(id) ON DELETE CASCADE,
                position INTEGER NOT NULL, excerpt TEXT NOT NULL, PRIMARY KEY(book, position))"""
        )
        createTombstones(db)
    }

    private fun createTombstones(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE book_tombstones (id TEXT PRIMARY KEY NOT NULL)")
    }

    override fun onConfigure(db: SQLiteDatabase) = db.setForeignKeyConstraintsEnabled(true)
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 5) createTombstones(db)
        if (oldVersion < 2) db.execSQL("ALTER TABLE books ADD COLUMN deleted_at INTEGER NOT NULL DEFAULT 0")
        if (oldVersion < 4) db.execSQL("ALTER TABLE books ADD COLUMN word_offset INTEGER NOT NULL DEFAULT 0")
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE books ADD COLUMN words INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE books ADD COLUMN created INTEGER NOT NULL DEFAULT 0")
            db.execSQL("UPDATE books SET created = updated")
        }
    }

    /** Backfill imported books from 1.1.x once; do this on Dispatchers.IO, not the UI thread. */
    fun fillMissingWordCounts() {
        val missing = readableDatabase.query("books", arrayOf("id"), "words = 0",
            null, null, null, null).use { cursor ->
            buildList { while (cursor.moveToNext()) add(cursor.getString(0)) }
        }
        missing.forEach { id ->
            runCatching {
                val count = document(id).passes.sumOf { countDocumentWords(it.text) }.coerceAtLeast(1)
                writableDatabase.update("books", ContentValues().apply { put("words", count) },
                    "id = ?", arrayOf(id))
            }
        }
    }

    /** The original PDF is optional: imports predating 1.3.1 have text-only archives. */
    fun originalPdf(id: String): File? = File(folder, "$id.pdf").takeIf { it.isFile }

    fun saveOriginalPdf(id: String, source: File) {
        require(book(id)?.format == "PDF") { "Dokumen ini bukan PDF." }
        require(source.isFile && source.length() in 1L..(100L * 1024 * 1024))
        val atomic = AtomicFile(File(folder, "$id.pdf"))
        val stream = atomic.startWrite()
        try {
            source.inputStream().use { it.copyTo(stream, 65536) }
            atomic.finishWrite(stream)
            TuturiumCloud.markDirty(appContext)
        } catch (error: Exception) {
            atomic.failWrite(stream)
            throw error
        }
    }

    fun documentBytes(id: String): Long =
        File(folder, "$id.json").takeIf { it.isFile }?.length() ?: 0L

    fun books(): List<Book> = readableDatabase.query(
        "books", null, "deleted_at = 0", null, null, null, "updated DESC, title COLLATE NOCASE"
    ).use { cursor -> buildList { while (cursor.moveToNext()) add(cursor.book()) } }

    fun book(id: String): Book? = readableDatabase.query(
        "books", null, "id = ? AND deleted_at = 0", arrayOf(id), null, null, null
    ).use { if (it.moveToFirst()) it.book() else null }

    fun add(title: String, format: String, document: ReadingDocument): Book {
        require(document.passes.isNotEmpty()) { "Tidak ada teks yang dapat dibaca." }
        val id = UUID.randomUUID().toString()
        val file = documentFile(id)
        val json = JSONArray()
        document.sections.forEach { json.put(JSONObject().put("title", it.title).put("text", it.text)) }
        val output = file.startWrite()
        try {
            output.write(json.toString().toByteArray(Charsets.UTF_8))
            file.finishWrite(output)
        } catch (error: Exception) {
            file.failWrite(output)
            throw error
        }
        val now = System.currentTimeMillis()
        val words = document.passes.sumOf { countDocumentWords(it.text) }.coerceAtLeast(1)
        val book = Book(id, title.take(200), format, document.passes.size, 0, now, words, now)
        try {
            writableDatabase.insertOrThrow("books", null, ContentValues().apply {
                put("id", id)
                put("title", book.title)
                put("format", format)
                put("count", book.count)
                put("updated", book.updated)
                put("created", book.created)
                put("words", book.words)
            })
        } catch (error: Exception) {
            file.delete()
            throw error
        }
        TuturiumCloud.markDirty(appContext)
        return book
    }

    internal fun openDocumentJson(id: String): java.io.InputStream {
        require(UUID.fromString(id).toString() == id)
        return documentFile(id).openRead()
    }

    internal fun documentModifiedAt(id: String): Long = documentFile(id).baseFile.lastModified()

    fun document(id: String): ReadingDocument {
        require(UUID.fromString(id).toString() == id)
        val json = JSONArray(documentFile(id).readFully().toString(Charsets.UTF_8))
        return ReadingDocument(List(json.length()) {
            val item = json.getJSONObject(it)
            Section(item.getString("title"), item.getString("text"))
        })
    }

    internal suspend fun narrationDocument(id: String, detectLayout: Boolean): ReadingDocument {
        val document = document(id)
        val source = originalPdf(id) ?: return document
        document.hasPdfTextGeometry = true
        val margins = PdfMarginStore.read(appContext, id)
        if (detectLayout || !margins.empty) {
            document.narrationLayout = PdfNarrationStore.load(appContext, source, documentModifiedAt(id),
                document, margins, detectLayout)
        }
        return document
    }

    fun position(id: String, index: Int, wordOffset: Int = 0) {
        writableDatabase.update("books", ContentValues().apply {
            put("position", index.coerceAtLeast(0))
            put("word_offset", wordOffset.coerceAtLeast(0))
            put("updated", System.currentTimeMillis())
        }, "id = ? AND count >= ?", arrayOf(id, index.toString()))
        TuturiumCloud.markDirty(appContext)
    }

    fun rename(id: String, title: String) {
        require(title.isNotBlank())
        writableDatabase.update("books", ContentValues().apply {
            put("title", title.trim().take(200))
            put("updated", System.currentTimeMillis())
        }, "id = ?", arrayOf(id))
        TuturiumCloud.markDirty(appContext)
    }

    fun trashed(): List<Book> = readableDatabase.query(
        "books", null, "deleted_at > 0", null, null, null, "deleted_at DESC"
    ).use { cursor -> buildList { while (cursor.moveToNext()) add(cursor.book()) } }

    fun delete(id: String) {
        writableDatabase.update("books", ContentValues().apply {
            put("deleted_at", System.currentTimeMillis().coerceAtLeast(1L))
            put("updated", System.currentTimeMillis())
        }, "id = ?", arrayOf(id))
        TuturiumCloud.markDirty(appContext)
    }

    fun restore(id: String) {
        writableDatabase.update("books", ContentValues().apply {
            put("deleted_at", 0)
            put("updated", System.currentTimeMillis())
        }, "id = ?", arrayOf(id))
        TuturiumCloud.markDirty(appContext)
    }

    @Synchronized
    fun deletePermanently(id: String) {
        if (eraseBook(id, trashOnly = true)) TuturiumCloud.markDirty(appContext)
    }

    internal fun tombstones(): List<String> = readableDatabase.query(
        "book_tombstones", arrayOf("id"), null, null, null, null, "id"
    ).use { cursor -> buildList { while (cursor.moveToNext()) add(cursor.getString(0)) } }

    @Synchronized
    internal fun restoreTombstone(id: String) {
        require(UUID.fromString(id).toString() == id)
        eraseBook(id, trashOnly = false)
    }

    private fun eraseBook(id: String, trashOnly: Boolean): Boolean {
        val db = writableDatabase
        val removed: Boolean
        db.beginTransaction()
        try {
            val where = if (trashOnly) "id = ? AND deleted_at > 0" else "id = ?"
            removed = db.delete("books", where, arrayOf(id)) != 0
            if (removed || !trashOnly) {
                db.insertWithOnConflict("book_tombstones", null,
                    ContentValues().apply { put("id", id) }, SQLiteDatabase.CONFLICT_IGNORE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        if (removed) {
            documentFile(id).delete()
            File(folder, "$id.pdf").delete()
            PdfMarginStore.write(appContext, id, PdfMarginSettings())
        }
        return removed
    }

    fun bookmarks(id: String): List<Bookmark> = readableDatabase.query(
        "bookmarks", arrayOf("position", "excerpt"), "book = ?", arrayOf(id), null, null, "position"
    ).use { cursor -> buildList {
        while (cursor.moveToNext()) add(Bookmark(cursor.getInt(0), cursor.getString(1)))
    } }

    fun toggleBookmark(id: String, index: Int, text: String) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            if (db.delete("bookmarks", "book = ? AND position = ?", arrayOf(id, index.toString())) == 0) {
                db.insertOrThrow("bookmarks", null, ContentValues().apply {
                    put("book", id)
                    put("position", index)
                    put("excerpt", text.take(180))
                })
            }
            db.update("books", ContentValues().apply {
                put("updated", System.currentTimeMillis())
            }, "id = ?", arrayOf(id))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        TuturiumCloud.markDirty(appContext)
    }


    /** Restore a Drive snapshot without replacing a newer local edit. Book ids
     * remain stable, so bookmarks and positions survive device migration. */
    @Synchronized
    fun restoreCloudBook(
        incoming: Book, deleted: Boolean, sectionsJson: String,
        marks: List<Bookmark>, originalPdf: File?
    ): Boolean {
        require(UUID.fromString(incoming.id).toString() == incoming.id)
        val sections = JSONArray(sectionsJson)
        require(sections.length() in 1..10_000)
        for (i in 0 until sections.length()) {
            val item = sections.getJSONObject(i)
            require(item.has("title") && item.has("text"))
        }
        val db = writableDatabase
        if (db.query("book_tombstones", arrayOf("id"), "id = ?",
                arrayOf(incoming.id), null, null, null).use { it.moveToFirst() }) return false
        val old = db.query("books", arrayOf("updated"), "id = ?",
            arrayOf(incoming.id), null, null, null).use {
            if (it.moveToFirst()) it.getLong(0) else null
        }
        if (old != null && old >= incoming.updated) return false
        val output = documentFile(incoming.id)
        val stream = output.startWrite()
        try {
            stream.write(sectionsJson.toByteArray(Charsets.UTF_8))
            output.finishWrite(stream)
        } catch (e: Exception) {
            output.failWrite(stream)
            throw e
        }
        originalPdf?.takeIf { it.isFile && incoming.format == "PDF" }?.let { source ->
            val pdf = AtomicFile(File(folder, "${incoming.id}.pdf"))
            val target = pdf.startWrite()
            try {
                source.inputStream().use { it.copyTo(target, 65536) }
                pdf.finishWrite(target)
            } catch (e: Exception) {
                pdf.failWrite(target)
                throw e
            }
        }
        db.beginTransaction()
        try {
            val values = ContentValues().apply {
                put("id", incoming.id)
                put("title", incoming.title.take(200))
                put("format", incoming.format)
                put("count", incoming.count)
                put("position", incoming.position.coerceIn(0, incoming.count))
                put("word_offset", incoming.wordOffset.coerceAtLeast(0))
                put("words", incoming.words)
                put("created", incoming.created)
                put("updated", incoming.updated)
                put("deleted_at", if (deleted) incoming.updated.coerceAtLeast(1L) else 0L)
            }
            db.insertWithOnConflict("books", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            db.delete("bookmarks", "book = ?", arrayOf(incoming.id))
            marks.forEach { mark ->
                db.insertWithOnConflict("bookmarks", null, ContentValues().apply {
                    put("book", incoming.id)
                    put("position", mark.index)
                    put("excerpt", mark.excerpt.take(180))
                }, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return true
    }

    private fun documentFile(id: String) = AtomicFile(File(folder, "$id.json"))

    private fun Cursor.book() = Book(
        getString(getColumnIndexOrThrow("id")), getString(getColumnIndexOrThrow("title")),
        getString(getColumnIndexOrThrow("format")), getInt(getColumnIndexOrThrow("count")),
        getInt(getColumnIndexOrThrow("position")), getLong(getColumnIndexOrThrow("updated")),
        getInt(getColumnIndexOrThrow("words")), getLong(getColumnIndexOrThrow("created")),
        getInt(getColumnIndexOrThrow("word_offset"))
    )

    companion object {
        @Volatile private var instance: Library? = null
        fun get(context: Context): Library = instance ?: synchronized(this) {
            instance ?: Library(context.applicationContext).also { instance = it }
        }
    }
}
