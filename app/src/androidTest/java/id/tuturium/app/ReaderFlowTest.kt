package id.tuturium.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReaderFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @After fun cleanup() {
        val library = Library.get(compose.activity)
        library.books().filter { it.title == EXAMPLE }.forEach { library.delete(it.id) }
    }

    @Test fun createExampleNavigateBookmarkAndReturnToLibrary() {
        compose.onNodeWithText("Pustaka saya").assertIsDisplayed()
        compose.onNodeWithText("Buka contoh bacaan").performClick()
        compose.waitUntil(15_000) { compose.onAllNodesWithText(EXAMPLE).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Kalimat berikutnya").performClick()
        compose.onNodeWithText("2 / 4").assertIsDisplayed()
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
