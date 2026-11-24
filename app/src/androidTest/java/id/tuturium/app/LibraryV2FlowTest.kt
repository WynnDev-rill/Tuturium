package id.tuturium.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class LibraryV2FlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val folderName = "Uji " + UUID.randomUUID().toString().take(8)

    @After fun cleanup() {
        FolderStore.remove(compose.activity, folderName)
    }

    @Test fun holdingADocumentOpensItsOptionsAndRenamesIt() {
        val library = Library.get(compose.activity)
        val book = library.add("Dokumen tekan lama", "TXT",
            ReadingDocument(listOf(Section("Bab", "Teks uji tekan lama."))))
        try {
            compose.onNodeWithText("Perpustakaan").performClick()
            compose.waitUntil(15_000) {
                compose.onAllNodesWithText(book.title).fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText(book.title).performTouchInput { longClick() }
            compose.onNodeWithText("Ubah nama").performScrollTo().performClick()
            compose.onNode(hasSetTextAction()).performTextReplacement("Dokumen sudah berganti nama")
            compose.onNodeWithText("Simpan").performClick()
            compose.waitUntil(15_000) {
                compose.onAllNodesWithText("Dokumen sudah berganti nama").fetchSemanticsNodes().isNotEmpty()
            }
        } finally {
            library.delete(book.id)
            library.deletePermanently(book.id)
        }
    }

    @Test fun menuChangesLayoutAndSearchKeepsLibraryVisible() {
        compose.onNodeWithText("Perpustakaan").performClick()
        compose.onNodeWithContentDescription("Opsi perpustakaan").performClick()
        compose.onNodeWithText("Kotak Kecil").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Cari dokumen").performClick()
        compose.onNodeWithContentDescription("Tutup pencarian").assertIsDisplayed()
        compose.onNode(hasSetTextAction()).performTextInput("tidak ditemukan 123 xyz")
        compose.onNodeWithText("Tidak ada hasil pencarian.").assertIsDisplayed()
        compose.onNodeWithContentDescription("Tutup pencarian").performClick()
        compose.onAllNodesWithText("Perpustakaan").onFirst().assertIsDisplayed()
    }

    @Test fun podcastIsRemovedAndLaterFilterCanBeCleared() {
        compose.onNodeWithText("Perpustakaan").performClick()
        compose.onNodeWithText("Podcast").assertDoesNotExist()
        compose.onNodeWithContentDescription("Opsi perpustakaan").performClick()
        compose.onNodeWithText("Baca nanti").assertDoesNotExist()
        compose.onNodeWithText("Pilih beberapa").assertIsDisplayed()
        compose.onNodeWithText("Pilih beberapa").performClick()
        compose.onNodeWithContentDescription("Batal pilih").performClick()
        compose.onNodeWithContentDescription("Filter perpustakaan").performClick()
        compose.onNodeWithText("Baca nanti").performClick()
        compose.onNodeWithContentDescription("Hapus filter").assertIsDisplayed()
        compose.onNodeWithContentDescription("Hapus filter").performClick()
        compose.onNodeWithText("Baca nanti").assertDoesNotExist()
    }

    @Test fun createFolderThroughSheetAndSeeEmptyFolder() {
        compose.onNodeWithText("Perpustakaan").performClick()
        compose.onNodeWithContentDescription("Buat folder").performClick()
        compose.onNodeWithText("Buat Folder").assertIsDisplayed()
        compose.onNode(hasSetTextAction()).performTextInput(folderName)
        compose.onNodeWithText("Simpan").performClick()
        compose.onNodeWithText("Folder ini kosong. Impor atau pindahkan file ke sini.").assertIsDisplayed()
    }

    @Test fun folderPinAndFolderMenuAreVisible() {
        compose.onNodeWithText("Perpustakaan").performClick()
        compose.onNodeWithContentDescription("Buat folder").performClick()
        compose.onNode(hasSetTextAction()).performTextInput(folderName)
        compose.onNodeWithText("Simpan").performClick()
        compose.onNodeWithContentDescription("Opsi perpustakaan").performClick()
        compose.onNodeWithText("Ubah Nama Folder").assertIsDisplayed()
        compose.onNodeWithText("Kelompokkan berdasarkan tanggal").performScrollTo().assertIsDisplayed()
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK)
        compose.waitForIdle()
        compose.onNodeWithContentDescription("Daftar folder").performClick()
        compose.onNodeWithContentDescription("Opsi folder $folderName").performClick()
        compose.onNodeWithText("Sematkan di atas").performClick()
        compose.onAllNodesWithContentDescription("Disematkan").onFirst().assertIsDisplayed()
    }

    @Test fun folderMenuMovesFolderAndKeepsItsDocuments() {
        val context = compose.activity
        val library = Library.get(context)
        val destination = "$folderName tujuan"
        val book = library.add("Isi yang dipindahkan", "TXT", ReadingDocument(listOf(Section("Bab", "Baca."))))
        try {
            FolderStore.add(context, folderName)
            FolderStore.add(context, destination)
            FolderStore.assign(context, book.id, folderName)
            compose.onNodeWithText("Perpustakaan").performClick()
            compose.onNodeWithContentDescription("Opsi folder $folderName").performClick()
            compose.onNodeWithText("Pilih isi").assertIsDisplayed()
            compose.onNodeWithText("Pindahkan ke folder").performClick()
            compose.onAllNodesWithText(destination).onLast().performClick()
            compose.onNodeWithText("Pindahkan").performClick()
            compose.waitUntil(10_000) { FolderStore.parentOf(context, folderName) == destination }
            compose.onNodeWithText(folderName).assertDoesNotExist()
            compose.onNodeWithText(destination).performClick()
            compose.onNodeWithText(folderName).performClick()
            compose.onNodeWithText(book.title).assertIsDisplayed()
            org.junit.Assert.assertFalse(FolderStore.canMove(context, destination, folderName))
        } finally {
            FolderStore.remove(context, destination)
            library.delete(book.id)
            library.deletePermanently(book.id)
        }
    }

    @Test fun deletedScreenRestoresSelectedItems() {
        val library = Library.get(compose.activity)
        val title = "Dokumen untuk dipulihkan"
        val book = library.add(title, "TXT", ReadingDocument(listOf(Section("Bab", "Teks uji."))))
        try {
            library.delete(book.id)
            compose.onNodeWithContentDescription("Buka profil").performClick()
            compose.onNodeWithText("File yang Dihapus").performScrollTo().performClick()
            compose.onNodeWithText("Dihapus").assertIsDisplayed()
            compose.onNodeWithText("Pulihkan").assertIsNotEnabled()
            compose.onNodeWithText(title).performClick()
            compose.onNodeWithText("Pulihkan").performClick()
            compose.waitUntil(15_000) { library.book(book.id) != null }
            compose.onNodeWithText(title).assertDoesNotExist()
        } finally {
            library.delete(book.id)
            library.deletePermanently(book.id)
        }
    }

    @Test fun aboutOpensDedicatedPage() {
        compose.onNodeWithContentDescription("Buka profil").performClick()
        compose.onNodeWithText("Tentang Tuturium").performScrollTo().performClick()
        compose.onNodeWithText("Tentang Tuturium").assertIsDisplayed()
        compose.onNodeWithText("Versi ${BuildConfig.VERSION_NAME}").assertIsDisplayed()
    }

    @Test fun filedBookIsHiddenFromRootAndCanReturn() {
        val library = Library.get(compose.activity)
        val book = library.add("Bacaan pindah folder", "TXT",
            ReadingDocument(listOf(Section("Bab", "Contoh buku."))))
        try {
            FolderStore.add(compose.activity, folderName)
            FolderStore.assign(compose.activity, book.id, folderName)
            compose.onNodeWithText("Perpustakaan").performClick()
            compose.onNodeWithText(book.title).assertDoesNotExist()
            compose.onNodeWithText(folderName).performClick()
            compose.waitUntil(15_000) { compose.onAllNodesWithText(book.title).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText(book.title).performTouchInput { longClick() }
            compose.onNodeWithText("Pindahkan ke folder").performScrollTo().performClick()
            compose.onNodeWithText("Pindahkan").performClick()
            compose.onNodeWithText(book.title).assertDoesNotExist()
            compose.onNodeWithContentDescription("Daftar folder").performClick()
            compose.onNodeWithText(book.title).assertIsDisplayed()
        } finally {
            FolderStore.assign(compose.activity, book.id, null)
            library.delete(book.id)
            library.deletePermanently(book.id)
        }
    }

    @Test fun guestStartsWithoutGoogleAndSignInIsOptionalInProfile() {
        compose.onNodeWithText("Beranda").assertIsDisplayed()
        compose.onNodeWithContentDescription("Buka profil").performClick()
        compose.onNodeWithText("Akun tamu").assertIsDisplayed()
        compose.onNodeWithText("Akun").performClick()
        compose.onNodeWithText("Masuk dengan Google").assertIsDisplayed()
        compose.onNodeWithContentDescription("Kembali").performClick()
        compose.onNodeWithText("Penyimpanan & Data").performClick()
        compose.onNodeWithText("Penyimpanan lokal").assertIsDisplayed()
    }

    @Test fun guestAccountMenuIsAboveStatisticsAndCloudIsNotMandatory() {
        compose.onNodeWithContentDescription("Buka profil").performClick()
        compose.onNodeWithText("Akun").assertIsDisplayed()
        compose.onNodeWithText("Statistik").assertIsDisplayed()
        compose.onNodeWithText("Akun").performClick()
        compose.onNodeWithText("Akun tamu").assertIsDisplayed()
        compose.onNodeWithText("Masuk dengan Google").assertIsDisplayed()
        compose.onNodeWithText("Penyimpanan Google Drive").assertIsDisplayed()
        compose.onNodeWithContentDescription("Kembali").performClick()
        compose.onNodeWithText("Statistik").assertIsDisplayed()
    }

    @Test fun storageDashboardExplainsLocalStorage() {
        compose.onNodeWithContentDescription("Buka profil").performClick()
        compose.onNodeWithText("Penyimpanan & Data").performClick()
        compose.onNodeWithText("Penyimpanan lokal").assertIsDisplayed()
        compose.onNodeWithText("File bacaan").assertIsDisplayed()
        compose.onNodeWithText("Database & progres").assertIsDisplayed()
    }
    @Test fun signedInProfileSurvivesDriveAuthorizationLoss() {
        val context = compose.activity
        val photo = "https://example.invalid/avatar"
        try {
            compose.runOnIdle {
                TuturiumCloud.signIn(context, "example@example.invalid", "Pembaca", photo)
                TuturiumCloud.driveFailure("Google Drive memerlukan izin.")
                TuturiumCloud.signIn(context, "example@example.invalid", null, null)
                TuturiumCloud.driveFailure("Google Drive memerlukan izin.")
            }
            org.junit.Assert.assertEquals(photo, TuturiumCloud.state.value.photoUrl)
            compose.onNodeWithContentDescription("Buka profil").performClick()
            compose.onNodeWithText("Pembaca").assertIsDisplayed()
            compose.onNodeWithContentDescription("Foto profil Google").assertIsDisplayed()
            compose.onNodeWithText("Akun").performClick()
            compose.onNodeWithText("Gagal").assertIsDisplayed()
            compose.onNodeWithText("Hubungkan Google Drive").assertDoesNotExist()
            compose.onNodeWithText("Penyimpanan Google Drive").performClick()
            compose.onNodeWithText("Google Drive memerlukan izin.", substring = true).assertIsDisplayed()
            compose.onNodeWithText("Coba lagi").assertIsDisplayed()
        } finally {
            kotlinx.coroutines.runBlocking { TuturiumCloud.disconnect(context) }
        }
    }
}
