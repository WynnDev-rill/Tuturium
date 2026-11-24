package id.tuturium.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.core.view.WindowInsetsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
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
        compose.onNodeWithContentDescription("Putar").assertIsDisplayed()
        compose.onNodeWithContentDescription("Atur kecepatan").performClick()
        compose.onNodeWithContentDescription("Penggeser kecepatan").performTouchInput { swipeUp(durationMillis = 400) }
        compose.runOnIdle {
            assertTrue(ReadingSettings.read(compose.activity).speed > 2f)
        }
        compose.onNodeWithContentDescription("Penggeser kecepatan").performSemanticsAction(SemanticsActions.SetProgress) { it(2f) }
        compose.onNodeWithContentDescription("Percepat").performClick()
        assertTrue(compose.onAllNodesWithText("2,05×").fetchSemanticsNodes().isNotEmpty())
        compose.runOnIdle {
            assertTrue(kotlin.math.abs(ReadingSettings.read(compose.activity).speed - 2.05f) < 0.001f)
        }
        compose.onNodeWithContentDescription("Penggeser kecepatan").assertIsDisplayed()
        compose.onNodeWithContentDescription("Tingkatkan kecepatan otomatis").performClick()
        compose.onNodeWithText("+0,05× tiap 600 kata · hingga 4×").assertIsDisplayed()
        compose.onNodeWithContentDescription("Tingkatkan kecepatan otomatis").performClick()
        compose.onNodeWithContentDescription("Pilih 1,0×").performClick()
        compose.onNodeWithText("Selesai").assertDoesNotExist()
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK)
        compose.waitForIdle()
        compose.onNodeWithContentDescription("Opsi pembaca").performClick()
        compose.onNodeWithText("Tampilan").assertIsDisplayed().performClick()
        compose.onNodeWithText("Tampilan").assertIsDisplayed()
        compose.onNodeWithText("Warna sorotan").performClick()
        compose.onNodeWithContentDescription("Mint").performClick()
        compose.onNodeWithContentDescription("Sorot kalimat").performClick()
        compose.onNodeWithContentDescription("Kembali ke Tampilan").performClick()
        compose.onNodeWithText("Mint").assertIsDisplayed()
        compose.onNodeWithText("Fokus").performClick()
        compose.onNodeWithText("Font").performClick()
        compose.onNodeWithText("Crimson Pro").performClick()
        compose.onNodeWithContentDescription("Kembali ke Tampilan").performClick()
        compose.onNodeWithText("Crimson Pro").assertIsDisplayed()
        compose.onNodeWithText("Selesai").performClick()
        compose.onNodeWithContentDescription("Opsi pembaca").performClick()
        compose.onNodeWithText("Cari").performClick()
        val decor = compose.activity.window.decorView
        val ime = WindowInsetsCompat.Type.ime()
        compose.waitUntil(10_000) {
            androidx.core.view.ViewCompat.getRootWindowInsets(decor)?.isVisible(ime) == true
        }
        val keyboardTop = decor.height -
            checkNotNull(androidx.core.view.ViewCompat.getRootWindowInsets(decor)).getInsets(ime).bottom
        val searchBottom = compose.onNodeWithContentDescription("Cari di dokumen").fetchSemanticsNode().boundsInWindow.bottom
        val searchGapDp = (keyboardTop - searchBottom) / decor.resources.displayMetrics.density
        assertTrue("Search to keyboard gap: ${searchGapDp}dp", searchGapDp in 0f..64f)
        compose.onNodeWithContentDescription("Cari di dokumen").performTextInput("kalimat")
        compose.waitUntil(10_000) { compose.onAllNodesWithText("1/4").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Hasil berikutnya").performClick()
        compose.onNodeWithText("2/4").assertIsDisplayed()
        compose.onNodeWithContentDescription("Tutup pencarian").performClick()
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

    @Test fun supertonicOpensTheSharedVoiceDetail() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("offlineVoiceSmoke") == "true")
        val context = compose.activity.applicationContext
        val pack = OfflineCatalog.models.single()
        runBlocking { OfflineModelStore.install(context, pack) {} }
        compose.onNodeWithContentDescription("Buka profil").performClick()
        compose.onNodeWithText("Pustaka Suara").performClick()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Supertonic F1"))
        compose.onNodeWithText("Supertonic F1").performClick()
        compose.onNodeWithText("Pratinjau suara").assertIsDisplayed()
        compose.onNodeWithText("Pilih Supertonic F1").assertIsDisplayed()
        compose.onNodeWithContentDescription("Kembali ke pustaka suara").performClick()
        compose.onNodeWithText("Pustaka Suara").assertIsDisplayed()
    }

    @Test fun originalPdfScrollsVerticallyAndSwitchesToReadableText() {
        val context = compose.activity.applicationContext
        val library = Library.get(context)
        val book = library.add("Tata letak halaman", "PDF", ReadingDocument(listOf(
            Section("Halaman 1", "Teks halaman pertama."),
            Section("Halaman 3", "Teks halaman ketiga.")
        )))
        val file = java.io.File.createTempFile("original-pages-", ".pdf", context.cacheDir)
        try {
            val pdf = android.graphics.pdf.PdfDocument()
            try {
                repeat(3) { index ->
                    val page = pdf.startPage(android.graphics.pdf.PdfDocument.PageInfo.Builder(300, 440, index + 1).create())
                    page.canvas.drawText("Original ${index + 1}", 18f, 30f, android.graphics.Paint())
                    pdf.finishPage(page)
                }
                file.outputStream().use(pdf::writeTo)
            } finally {
                pdf.close()
            }
            library.saveOriginalPdf(book.id, file)
            compose.onNodeWithText("Perpustakaan").performClick()
            compose.waitUntil(15_000) { compose.onAllNodesWithText(book.title).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText(book.title).performClick()
            compose.waitUntil(15_000) {
                compose.onAllNodesWithContentDescription("Halaman PDF 1").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText("Halaman 1 dari 3").assertDoesNotExist()
            compose.onNodeWithContentDescription("Halaman berikutnya").assertDoesNotExist()
            compose.onNodeWithContentDescription("Halaman PDF 1").assertHasClickAction()
            val pdfList = compose.onNodeWithContentDescription("Dokumen PDF")
            pdfList.performTouchInput { swipeUp(durationMillis = 480) }
            compose.waitUntil(15_000) {
                compose.onAllNodesWithContentDescription("Halaman PDF 2").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithContentDescription("Halaman PDF 2").assertIsDisplayed().assertHasNoClickAction()
            pdfList.performScrollToIndex(2)
            compose.waitUntil(15_000) {
                compose.onAllNodesWithContentDescription("Halaman PDF 3").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithContentDescription("Halaman PDF 3").assertIsDisplayed().assertHasClickAction()
            compose.onNodeWithText("Teks").performClick()
            compose.onNodeWithText("Teks halaman pertama.").assertIsDisplayed()
        } finally {
            file.delete()
            library.delete(book.id)
            library.deletePermanently(book.id)
        }
    }

    companion object { private const val EXAMPLE = "Sepucuk surat untuk pembaca" }
}
