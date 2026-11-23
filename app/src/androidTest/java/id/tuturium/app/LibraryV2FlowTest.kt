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

    @Test fun menuChangesLayoutAndSearchKeepsLibraryVisible() {
        compose.onNodeWithText("Perpustakaan").performClick()
        compose.onNodeWithContentDescription("Opsi perpustakaan").performClick()
        compose.onNodeWithText("Kotak Kecil").performClick()
        compose.onNodeWithContentDescription("Cari dokumen").performClick()
        compose.onNodeWithContentDescription("Tutup pencarian").assertIsDisplayed()
        compose.onNode(hasSetTextAction()).performTextInput("tidak ditemukan 123 xyz")
        compose.onNodeWithText("Tidak ada hasil pencarian.").assertIsDisplayed()
        compose.onNodeWithContentDescription("Tutup pencarian").performClick()
        compose.onAllNodesWithText("Perpustakaan").onFirst().assertIsDisplayed()
    }

    @Test fun createFolderThroughSheetAndSeeEmptyFolder() {
        compose.onNodeWithText("Perpustakaan").performClick()
        compose.onNodeWithContentDescription("Buat folder").performClick()
        compose.onNodeWithText("Buat Folder").assertIsDisplayed()
        compose.onNode(hasSetTextAction()).performTextInput(folderName)
        compose.onNodeWithText("Simpan").performClick()
        compose.onNodeWithText("Folder ini kosong. Impor atau pindahkan file ke sini.").assertIsDisplayed()
    }

    @Test fun guestStartsWithoutGoogleAndSignInIsOptionalInProfile() {
        compose.onNodeWithText("Beranda").assertIsDisplayed()
        compose.onNodeWithContentDescription("Profil tamu").performClick()
        compose.onNodeWithText("Akun tamu").assertIsDisplayed()
        compose.onNodeWithText("Akun").performClick()
        compose.onNodeWithText("Masuk dengan Google").assertIsDisplayed()
        compose.onNodeWithContentDescription("Kembali").performClick()
        compose.onNodeWithText("Penyimpanan & Data").performClick()
        compose.onNodeWithText("Penyimpanan lokal").assertIsDisplayed()
    }

    @Test fun guestAccountMenuIsAboveStatisticsAndCloudIsNotMandatory() {
        compose.onNodeWithContentDescription("Profil tamu").performClick()
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
        compose.onNodeWithContentDescription("Profil tamu").performClick()
        compose.onNodeWithText("Penyimpanan & Data").performClick()
        compose.onNodeWithText("Penyimpanan lokal").assertIsDisplayed()
        compose.onNodeWithText("File bacaan").assertIsDisplayed()
        compose.onNodeWithText("Database & progres").assertIsDisplayed()
    }
}
