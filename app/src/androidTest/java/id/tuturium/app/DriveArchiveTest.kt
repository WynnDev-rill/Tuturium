package id.tuturium.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DriveArchiveTest {
    @Test fun exportsAndRestoresBooksAndBookmarks() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val library = Library.get(context)
        val book = library.add("Drive migration check", "TXT",
            ReadingDocument(listOf(Section("Bab 1", "Ini contoh data yang harus pulih."))))
        library.toggleBookmark(book.id, 0, "Ini contoh")
        library.position(book.id, 0, 4)
        assertEquals(4, library.book(book.id)?.wordOffset)
        val archive = File.createTempFile("tuturium-drive-", ".zip", context.cacheDir)
        try {
            DriveArchive(context).export(archive)
            library.delete(book.id)
            library.deletePermanently(book.id)
            assertNull(library.book(book.id))
            val imported = DriveArchive(context).restore(archive)
            assertTrue(imported >= 1)
            assertEquals(book.title, library.book(book.id)?.title)
            assertEquals("Ini contoh data yang harus pulih.",
                library.document(book.id).sections.first().text)
            assertEquals(1, library.bookmarks(book.id).size)
            assertEquals(0, library.book(book.id)?.position)
            assertEquals(4, library.book(book.id)?.wordOffset)
        } finally {
            archive.delete()
            library.delete(book.id)
            library.deletePermanently(book.id)
        }
    }

    @Test fun rejectsArchiveWithUnexpectedEntries() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val archive = File.createTempFile("unsafe-tuturium-", ".zip", context.cacheDir)
        try {
            java.util.zip.ZipOutputStream(archive.outputStream()).use { zip ->
                zip.putNextEntry(java.util.zip.ZipEntry("settings.xml"))
                zip.write("bad".toByteArray())
                zip.closeEntry()
            }
            try {
                DriveArchive(context).restore(archive)
                fail("Untrusted archive must not be restored")
            } catch (_: IllegalArgumentException) {
                // Archive data must never become arbitrary filesystem paths.
            }
        } finally {
            archive.delete()
        }
    }
}
