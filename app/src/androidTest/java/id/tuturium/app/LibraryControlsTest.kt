package id.tuturium.app

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class LibraryControlsTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val library = Library.get(context)

    @Test fun importedBooksHaveWordCountsAndMigrationBackfills() {
        val book = library.add("Word count test", "TXT",
            ReadingDocument(listOf(Section("Test", "Satu dua tiga. Empat lima enam."))))
        try {
            assertEquals(6, library.book(book.id)?.words)
            assertTrue((library.book(book.id)?.created ?: 0L) > 0L)
            library.writableDatabase.execSQL("UPDATE books SET words=0 WHERE id=?",
                arrayOf(book.id))
            library.fillMissingWordCounts()
            assertEquals(6, library.book(book.id)?.words)
        } finally {
            library.delete(book.id); library.deletePermanently(book.id)
        }
    }

    @Test fun renamingAndRemovingFoldersPreservesDocuments() {
        val name = "Test Folder ${UUID.randomUUID()}"
        val renamed = name + " Baru"
        val book = library.add("Keep me", "TXT",
            ReadingDocument(listOf(Section("Test", "Dokumen harus tetap aman."))))
        try {
            assertTrue(FolderStore.add(context, name))
            FolderStore.assign(context, book.id, name)
            assertTrue(FolderStore.rename(context, name, renamed))
            assertEquals(renamed, FolderStore.folderOf(context, book.id))
            FolderStore.remove(context, renamed)
            assertNull(FolderStore.folderOf(context, book.id))
            assertNotNull(library.book(book.id))
        } finally {
            FolderStore.remove(context, name)
            FolderStore.remove(context, renamed)
            library.delete(book.id); library.deletePermanently(book.id)
        }
    }

    @Test fun removingCacheNeverDeletesAnImportedBook() {
        val book = library.add("Cache safety", "TXT",
            ReadingDocument(listOf(Section("Test", "Semua data tetap lokal."))))
        val cache = File(context.cacheDir, "shared/test-${UUID.randomUUID()}.txt")
        try {
            cache.parentFile?.mkdirs()
            cache.writeText("temp")
            val before = StorageInspector.read(context)
            assertTrue(before.documents > 0)
            assertTrue(before.cache >= 4)
            StorageInspector.clearTemporary(context)
            assertFalse(cache.exists())
            assertNotNull(library.book(book.id))
            assertEquals("Semua data tetap lokal.", library.document(book.id).sections.first().text)
        } finally {
            library.delete(book.id); library.deletePermanently(book.id)
            cache.delete()
        }
    }

    @Test fun pinAndListeningQueuePersist() {
        val id = UUID.randomUUID().toString()
        val another = UUID.randomUUID().toString()
        try {
            BookActions.togglePin(context, id)
            assertTrue(BookActions.pinned(context, id))
            BookActions.enqueue(context, id)
            BookActions.enqueue(context, another, next = true)
            assertEquals(another, BookActions.dequeue(context))
            assertEquals(id, BookActions.dequeue(context))
        } finally {
            if (BookActions.pinned(context, id)) BookActions.togglePin(context, id)
            BookActions.remove(context, id)
            BookActions.remove(context, another)
        }
    }
}
