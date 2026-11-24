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

    @Test fun pdfReturnDetectsWordOutsideViewportEvenOnSamePage() {
        assertFalse(pdfWordOutsideViewport(100f, 130f, 0f, 500f))
        assertTrue(pdfWordOutsideViewport(520f, 550f, 0f, 500f))
        assertTrue(pdfWordOutsideViewport(-50f, -20f, 0f, 500f))
    }
}
