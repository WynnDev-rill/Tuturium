package id.tuturium.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.json.JSONArray
import org.json.JSONObject
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
            // A new device has no row or tombstone for this UUID.
            library.writableDatabase.delete("books", "id = ?", arrayOf(book.id))
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

    @Test fun unchangedArchiveIsStableAndHardDeleteWinsOverStaleBackup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val library = Library.get(context)
        val archive = DriveArchive(context)
        val first = File.createTempFile("tuturium-stale-", ".zip", context.cacheDir)
        val second = File.createTempFile("tuturium-repeat-", ".zip", context.cacheDir)
        val book = library.add("Permanently deleted", "TXT",
            ReadingDocument(listOf(Section("Bab", "A book must never return after a hard delete."))))
        try {
            archive.export(first)
            archive.export(second)
            assertEquals(DriveAppDataClient.checksum(first), DriveAppDataClient.checksum(second))
            library.delete(book.id)
            library.deletePermanently(book.id)
            archive.restore(first)
            assertNull(library.book(book.id))
            assertTrue(book.id in library.tombstones())
        } finally {
            first.delete()
            second.delete()
            library.delete(book.id)
            library.deletePermanently(book.id)
        }
    }

    @Test fun invalidLaterBookDoesNotRestoreEarlierBookOrApplyDeletion() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val library = Library.get(context)
        val local = library.add("Buku lokal", "TXT",
            ReadingDocument(listOf(Section("Bab", "Buku ini harus tetap tersedia."))))
        val firstId = UUID.randomUUID().toString()
        val secondId = UUID.randomUUID().toString()
        val archive = archiveWith(context.cacheDir,
            listOf(record(firstId), record(secondId)),
            mapOf(firstId to """[{"title":"Bab","text":"Data yang valid."}]""",
                secondId to """[{"title":"Bab","text":null}]"""),
            tombstones = listOf(local.id))
        try {
            assertThrows(IllegalArgumentException::class.java) { DriveArchive(context).restore(archive) }
            assertNull("A valid earlier record must not be applied", library.book(firstId))
            assertNotNull("The invalid archive must not erase local books", library.book(local.id))
        } finally {
            archive.delete()
            for (id in listOf(local.id, firstId, secondId)) {
                library.delete(id)
                library.deletePermanently(id)
            }
        }
    }

    @Test fun missingPdfRecoversWithoutRewindingProgressOrBookmarks() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val library = Library.get(context)
        val local = library.add("Nama lokal", "PDF",
            ReadingDocument(listOf(Section("Bab", "Pertahankan progres dan markah lokal."))))
        val pdfBytes = "%PDF-1.4\n%%EOF".toByteArray()
        val archive = archiveWith(context.cacheDir, listOf(record(local.id).put("format", "PDF")),
            mapOf(local.id to """[{"title":"Bab","text":"Pertahankan progres dan markah lokal."}]"""),
            pdfs = mapOf(local.id to pdfBytes))
        library.position(local.id, 0, 5)
        library.toggleBookmark(local.id, 0, "Markah lokal")
        try {
            val before = library.book(local.id)
            assertNull(library.originalPdf(local.id))
            DriveArchive(context).restore(archive)
            assertArrayEquals(pdfBytes, library.originalPdf(local.id)?.readBytes())
            assertEquals(before, library.book(local.id))
            assertEquals(listOf(Bookmark(0, "Markah lokal")), library.bookmarks(local.id))
        } finally {
            archive.delete()
            library.delete(local.id)
            library.deletePermanently(local.id)
        }
    }

    @Test fun syncedMembershipCannotHideABookWhenLocalFolderListWins() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val id = UUID.randomUUID().toString()
        val folderPreferences = context.getSharedPreferences("library_folders", 0)
        val previous = folderPreferences.getStringSet("names", null)?.toSet()
        val localName = "Lokal-$id"
        val remoteName = "Cloud-$id"
        val storedMembership = "book:$id"
        folderPreferences.edit().putStringSet("names", setOf(localName))
            .putString(storedMembership, remoteName).commit()
        try {
            assertTrue(isInLibraryFolder(FolderStore.folderOf(context, id), displayed = null))
            folderPreferences.edit().putStringSet("names", setOf(localName, remoteName)).commit()
            assertEquals(remoteName, FolderStore.folderOf(context, id))
        } finally {
            folderPreferences.edit().putStringSet("names", previous).remove(storedMembership).commit()
        }
    }

    private fun record(id: String) = JSONObject().put("id", id).put("title", "Judul dari Drive")
        .put("format", "TXT").put("count", 1).put("position", 0).put("updated", 1L)

    private fun archiveWith(
        directory: File, records: List<JSONObject>, documents: Map<String, String>,
        tombstones: List<String> = emptyList(), pdfs: Map<String, ByteArray> = emptyMap()
    ): File = File.createTempFile("tuturium-audit-", ".zip", directory).also { archive ->
        ZipOutputStream(archive.outputStream()).use { zip ->
            fun write(name: String, value: ByteArray) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(value)
                zip.closeEntry()
            }
            val manifest = JSONObject().put("schema", 1).put("books", JSONArray(records))
                .put("tombstones", JSONArray(tombstones))
            write("manifest.json", manifest.toString().toByteArray())
            documents.forEach { (id, json) -> write("books/$id.json", json.toByteArray()) }
            pdfs.forEach { (id, data) -> write("books/$id.pdf", data) }
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
