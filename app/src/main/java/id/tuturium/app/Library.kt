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
    val updated: Long
)

data class Bookmark(val index: Int, val excerpt: String)

class Library private constructor(context: Context) :
    SQLiteOpenHelper(context, "library.db", null, 1) {
    private val folder = File(context.filesDir, "books").apply { mkdirs() }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE books (id TEXT PRIMARY KEY, title TEXT NOT NULL, format TEXT NOT NULL,
                count INTEGER NOT NULL, position INTEGER NOT NULL DEFAULT 0, updated INTEGER NOT NULL)"""
        )
        db.execSQL(
            """CREATE TABLE bookmarks (book TEXT REFERENCES books(id) ON DELETE CASCADE,
                position INTEGER NOT NULL, excerpt TEXT NOT NULL, PRIMARY KEY(book, position))"""
        )
    }

    override fun onConfigure(db: SQLiteDatabase) = db.setForeignKeyConstraintsEnabled(true)
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun books(): List<Book> = readableDatabase.query(
        "books", null, null, null, null, null, "updated DESC, title COLLATE NOCASE"
    ).use { cursor -> buildList { while (cursor.moveToNext()) add(cursor.book()) } }

    fun book(id: String): Book? = readableDatabase.query(
        "books", null, "id = ?", arrayOf(id), null, null, null
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
        val book = Book(id, title.take(200), format, document.passes.size, 0, System.currentTimeMillis())
        try {
            writableDatabase.insertOrThrow("books", null, ContentValues().apply {
                put("id", id)
                put("title", book.title)
                put("format", format)
                put("count", book.count)
                put("updated", book.updated)
            })
        } catch (error: Exception) {
            file.delete()
            throw error
        }
        return book
    }

    fun document(id: String): ReadingDocument {
        require(UUID.fromString(id).toString() == id)
        val json = JSONArray(documentFile(id).readFully().toString(Charsets.UTF_8))
        return ReadingDocument(List(json.length()) {
            val item = json.getJSONObject(it)
            Section(item.getString("title"), item.getString("text"))
        })
    }

    fun position(id: String, index: Int) {
        writableDatabase.update("books", ContentValues().apply {
            put("position", index)
            put("updated", System.currentTimeMillis())
        }, "id = ? AND count >= ?", arrayOf(id, index.toString()))
    }

    fun rename(id: String, title: String) {
        require(title.isNotBlank())
        writableDatabase.update("books", ContentValues().apply { put("title", title.trim().take(200)) },
            "id = ?", arrayOf(id))
    }

    fun delete(id: String) {
        if (writableDatabase.delete("books", "id = ?", arrayOf(id)) != 0) documentFile(id).delete()
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
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun documentFile(id: String) = AtomicFile(File(folder, "$id.json"))

    private fun Cursor.book() = Book(
        getString(getColumnIndexOrThrow("id")), getString(getColumnIndexOrThrow("title")),
        getString(getColumnIndexOrThrow("format")), getInt(getColumnIndexOrThrow("count")),
        getInt(getColumnIndexOrThrow("position")), getLong(getColumnIndexOrThrow("updated"))
    )

    companion object {
        @Volatile private var instance: Library? = null
        fun get(context: Context): Library = instance ?: synchronized(this) {
            instance ?: Library(context.applicationContext).also { instance = it }
        }
    }
}
