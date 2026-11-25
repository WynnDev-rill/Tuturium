package id.tuturium.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.test.pinch
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

    private fun openSpeedControls() {
        val decor = compose.activity.window.decorView
        val ime = WindowInsetsCompat.Type.ime()
        compose.waitUntil(10_000) {
            androidx.core.view.ViewCompat.getRootWindowInsets(decor)?.isVisible(ime) == false
        }
        val speed = compose.onNodeWithContentDescription("Atur kecepatan")
        compose.waitUntil(10_000) { runCatching { speed.assertIsDisplayed() }.isSuccess }
        speed.performClick()
        val slider = compose.onNodeWithContentDescription("Penggeser kecepatan")
        compose.waitUntil(10_000) { runCatching { slider.assertIsDisplayed() }.isSuccess }
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

    @Test fun readerCanExitAfterItsBookIsRemovedByCloudSync() {
        val library = Library.get(compose.activity)
        val book = library.add("Dokumen terhapus dari Drive", "TXT",
            ReadingDocument(listOf(Section("Bab", "Bacaan sementara sebelum disinkronkan."))))
        try {
            compose.onNodeWithText("Perpustakaan").performClick()
            compose.waitUntil(15_000) {
                compose.onAllNodesWithText(book.title).fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText(book.title).performClick()
            compose.waitUntil(15_000) {
                compose.onAllNodesWithContentDescription("Kembali ke pustaka").fetchSemanticsNodes().isNotEmpty()
            }
            library.restoreTombstone(book.id)
            compose.onNodeWithContentDescription("Kembali ke pustaka").performClick()
            compose.waitUntil(15_000) {
                compose.onAllNodesWithContentDescription("Kembali ke pustaka").fetchSemanticsNodes().isEmpty() &&
                    compose.onAllNodesWithText(book.title).fetchSemanticsNodes().isEmpty() &&
                    compose.onAllNodesWithText("Perpustakaan").fetchSemanticsNodes().isNotEmpty()
            }
        } finally {
            library.delete(book.id)
            library.deletePermanently(book.id)
        }
    }

    @Test fun libraryAndReaderAgreeOnSavedWordsAndRemainingDuration() {
        val library = Library.get(compose.activity)
        val book = library.add("Uji durasi dengan progres", "TXT", ReadingDocument(listOf(
            Section("Bab", "Halo!\nSatu dua tiga empat.")
        )))
        library.position(book.id, 1, 5)
        try {
            compose.onNodeWithText("Perpustakaan").performClick()
            compose.waitUntil(15_000) {
                compose.onAllNodesWithText("40% · ≈1 menit · TXT").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText(book.title).performClick()
            val timeInReader = hasText("≈ ", substring = true) and
                hasText(" / ", substring = true)
            compose.waitUntil(15_000) {
                compose.onAllNodes(timeInReader).fetchSemanticsNodes().isNotEmpty()
            }
            val readerLabel = compose.onNode(timeInReader).fetchSemanticsNode()
                .config[androidx.compose.ui.semantics.SemanticsProperties.Text].single().text
            val times = readerLabel.removePrefix("≈ ").split(" / ")
            fun parseTime(value: String): Int = value.split(":").fold(0) { acc, part -> acc * 60 + part.toInt() }
            assertTrue(times.size == 2)
            val elapsed = parseTime(times[0])
            val total = parseTime(times[1])
            assertTrue(elapsed in 0..total)
            openSpeedControls()
            val remaining = compose.onNodeWithText("≈${formatPlaybackTime(total - elapsed)} tersisa")
            compose.waitUntil(10_000) { runCatching { remaining.assertIsDisplayed() }.isSuccess }
            remaining.assertIsDisplayed()
        } finally {
            library.delete(book.id)
            library.deletePermanently(book.id)
        }
    }

    @Test fun pdfMarginEditorPersistsAnAreaAndHidesControlsInTextMode() {
        val context = compose.activity
        val library = Library.get(context)
        val source = java.io.File.createTempFile("reader-area-", ".pdf", context.cacheDir)
        com.tom_roush.pdfbox.android.PDFBoxResourceLoader.init(context)
        com.tom_roush.pdfbox.pdmodel.PDDocument().use { pdf ->
            val page = com.tom_roush.pdfbox.pdmodel.PDPage().also(pdf::addPage)
            com.tom_roush.pdfbox.pdmodel.PDPageContentStream(pdf, page).use {
                it.beginText()
                it.setFont(com.tom_roush.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 16f)
                it.newLineAtOffset(50f, 500f)
                it.showText("Isi buku dibaca.")
                it.endText()
            }
            pdf.save(source)
        }
        val book = library.add("Dokumen uji margin", "PDF",
            ReadingDocument(listOf(Section("Halaman 1", "Isi buku dibaca."))))
        try {
            library.saveOriginalPdf(book.id, source)
            compose.onNodeWithText("Perpustakaan").performClick()
            compose.waitUntil(15_000) {
                compose.onAllNodesWithText(book.title).fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText(book.title).performClick()
            compose.onNodeWithContentDescription("Opsi pembaca").performClick()
            compose.onNodeWithText("Area bacaan PDF").performClick()
            compose.waitUntil(15_000) {
                compose.onAllNodesWithContentDescription("Margin atas").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText("Semua halaman").performClick()
            compose.onNodeWithContentDescription("Margin atas")
                .performSemanticsAction(SemanticsActions.SetProgress) { it(.25f) }
            compose.onNodeWithText("Simpan").performClick()
            assertTrue(PdfMarginStore.read(context, book.id).all.top in 0.24f..0.26f)
            compose.onNodeWithText("Teks").performClick()
            compose.onNodeWithContentDescription("Opsi pembaca").performClick()
            compose.onNodeWithText("Area bacaan PDF").assertDoesNotExist()
        } finally {
            library.delete(book.id)
            library.deletePermanently(book.id)
            source.delete()
        }
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
        openSpeedControls()
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
            compose.onNodeWithText("1").assertIsDisplayed()
            compose.onNodeWithText("1 dari 3").assertDoesNotExist()
            compose.onNodeWithContentDescription("Halaman berikutnya").assertDoesNotExist()
            compose.onNodeWithContentDescription("Halaman PDF 1").assertHasClickAction()
            val pdfList = compose.onNodeWithContentDescription("Dokumen PDF")
            pdfList.performTouchInput {
                down(center)
                advanceEventTime(80)
                cancel()
            }
            compose.onNodeWithText("1 dari 3").assertIsDisplayed()
            compose.waitUntil(5_000) {
                compose.onAllNodesWithText("1 dari 3").fetchSemanticsNodes().isEmpty()
            }
            fun zoomPercent(): Int =
                pdfList.fetchSemanticsNode().config[SemanticsProperties.StateDescription]
                    .substringAfter("Pembesaran ").substringBefore(" persen").toInt()
            fun pinchBy(from: Float, to: Float) {
                pdfList.performTouchInput {
                    val inner = width * from
                    val outer = width * to
                    pinch(
                        start0 = center - Offset(inner, 0f), end0 = center - Offset(outer, 0f),
                        start1 = center + Offset(inner, 0f), end1 = center + Offset(outer, 0f),
                        durationMillis = 500
                    )
                }
            }
            pinchBy(.10f, .19f)
            compose.waitUntil(5_000) { zoomPercent() > 130 }
            compose.onNodeWithContentDescription("Halaman PDF 1").assertIsDisplayed()
            pinchBy(.20f, .06f)
            compose.waitUntil(5_000) { zoomPercent() < 85 }
            val fittedZoom = zoomPercent()
            pinchBy(.10f, 10f / fittedZoom)
            compose.waitUntil(5_000) { zoomPercent() in 95..105 }
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
