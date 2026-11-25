package id.tuturium.app

import org.junit.Assert.*
import org.junit.Test

class NarrationFlowTest {
    @Test fun longChunkPrefersAClauseAndRetainsEverySourceCharacter() {
        val first = "Ini contoh klausa yang selesai,"
        val source = "$first teks berikutnya dapat dibaca langsung tanpa satu kata pun hilang."
        val ranges = synthesisRanges(source, 48)
        assertEquals(first, source.substring(ranges.first()))
        assertEquals(source.filterNot(Char::isWhitespace),
            ranges.joinToString("") { source.substring(it) }.filterNot(Char::isWhitespace))
    }

    @Test fun commonAbbreviationsAndPdfTypographyKeepSourceOffsets() {
        val source = "dr. Rina ke hlm. 12, ﬂora dan kata\u00addasar."
        val prepared = PreparedSpeech(source, IntArray(source.length) { it }).normalizedForNarration()
        assertEquals("dokter Rina ke halaman dua belas, flora dan katadasar.", prepared.text)
        assertEquals(source.indexOf("Rina"), prepared.offsets[prepared.text.indexOf("Rina")])
        assertEquals(source.indexOf("12"), prepared.offsets[prepared.text.indexOf("dua belas")])
    }

    @Test fun pageContinuationDoesNotMergeStableBookmarksAndParagraphPausesStayDistinct() {
        val document = ReadingDocument(listOf(Section("Halaman 1", "Ia berjalan menuju"),
            Section("Halaman 2", "penginapan. Hujan deras.\nIa beristirahat.")))
        val options = ListeningOptions(sentencePauseMs = 100, paragraphPauseMs = 300)
        assertEquals(listOf(0, 1, 1, 1), document.passes.indices.map(document::pdfPageForPass))
        assertTrue(document.continuesAcrossPdfPage(0))
        assertEquals(12, document.pauseAfter(0, options).totalMs)
        assertEquals(212, document.pauseAfter(1, options).totalMs)
        assertEquals(480, document.pauseAfter(2, options).totalMs)
        assertEquals(0, document.pauseAfter(3, options).totalMs)
        val missingPage = ReadingDocument(listOf(Section("Halaman 2", "Ia berjalan menuju"),
            Section("Halaman 4", "penginapan.")))
        assertFalse(missingPage.continuesAcrossPdfPage(0))
    }

    @Test fun trimPreservesInteriorSilenceAndAProtectiveMargin() {
        val pcm = FloatArray(3_000)
        pcm.fill(.02f, 450, 900)
        pcm.fill(-.02f, 1550, 2400)
        val range = spokenPcmRange(pcm, 1000)
        assertEquals(430, range.first)
        assertEquals(2419, range.last)
        assertTrue(1000 in range)
        assertEquals(0..99, spokenPcmRange(FloatArray(100), 44_100))
    }
}
