package id.tuturium.app

import org.junit.Assert.*
import org.junit.Test

class ReaderFolderRegressionTest {
    @Test fun rootOnlyShowsUnfiledBooks() {
        assertTrue(isInLibraryFolder(null, null))
        assertFalse(isInLibraryFolder("Novel", null))
        assertTrue(isInLibraryFolder("Novel", "Novel"))
        assertFalse(isInLibraryFolder(null, "Novel"))
    }

    @Test fun pdfPinchKeepsThePointBeneathTheFingersAndClampsTheEdges() {
        assertEquals(-100f, pdfPanForZoom(0f, 400, 300f, 300f, 2f, 2f), .01f)
        assertEquals(-65f, pdfPanForZoom(-100f, 400, 300f, 335f, 2f, 1f), .01f)
        assertEquals(200f, clampPdfOffset(420f, 400, 2f), .01f)
        assertEquals(-200f, clampPdfOffset(-420f, 400, 2f), .01f)
        assertEquals(0f, clampPdfOffset(100f, 400, .70f), .01f)
    }

    @Test fun pdfReturnDetectsWordOutsideViewportEvenOnSamePage() {
        assertFalse(pdfWordOutsideViewport(100f, 130f, 0f, 500f))
        assertTrue(pdfWordOutsideViewport(520f, 550f, 0f, 500f))
        assertTrue(pdfWordOutsideViewport(-50f, -20f, 0f, 500f))
    }
}
