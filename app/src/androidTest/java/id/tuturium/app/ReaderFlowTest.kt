package id.tuturium.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.core.view.WindowInsetsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReaderFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @After fun cleanup() {
        val library = Library.get(compose.activity)
        library.books().filter { it.title == EXAMPLE }.forEach {
            library.delete(it.id)
            library.deletePermanently(it.id)
        }
    }

    @Test fun bottomNavigationClearsSystemButtons() {
        compose.onNodeWithText("Beranda").assertIsDisplayed()
        compose.onNodeWithText("Perpustakaan").assertIsDisplayed()
        val decor = compose.activity.window.decorView
        val insets = WindowInsetsCompat.toWindowInsetsCompat(
            requireNotNull(decor.rootWindowInsets), decor
        ).getInsets(WindowInsetsCompat.Type.navigationBars())
        val bottom = compose.onNodeWithText("Beranda").fetchSemanticsNode().boundsInWindow.bottom
        assertTrue("Bottom bar label overlaps Android system navigation: bottom=$bottom, inset=${insets.bottom}",
            bottom <= decor.height - insets.bottom + 2)
        compose.onNodeWithText("Perpustakaan").performClick()
        compose.onNodeWithText("Beranda").performClick()
        compose.onNodeWithText("Lanjutkan").assertIsDisplayed()
    }

    @Test fun createExampleNavigateBookmarkAndReturnToHome() {
        compose.onNodeWithText("Lanjutkan").assertIsDisplayed()
        compose.onNodeWithText("Tempel").performClick()
        compose.onNodeWithText("Judul (opsional)").performTextInput(EXAMPLE)
        compose.onNodeWithText("Teks bacaan").performTextInput(
            "Kalimat pertama. Kalimat kedua. Kalimat ketiga. Kalimat keempat.")
        compose.onNodeWithText("Simpan").performClick()
        compose.waitUntil(15_000) { compose.onAllNodesWithText(EXAMPLE).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Maju sekitar 10 detik").performClick()
        compose.onNodeWithContentDescription("Simpan markah").performClick()
        compose.waitUntil(15_000) {
            compose.onAllNodes(hasContentDescription("Hapus markah")).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription("Hapus markah").assertIsDisplayed()
        compose.onNodeWithContentDescription("Kembali ke pustaka").performClick()
        compose.waitUntil(15_000) { compose.onAllNodes(hasText(EXAMPLE)).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(EXAMPLE).assertIsDisplayed()
    }

    companion object { private const val EXAMPLE = "Sepucuk surat untuk pembaca" }
}
