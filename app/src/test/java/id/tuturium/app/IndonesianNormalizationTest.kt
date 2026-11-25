package id.tuturium.app

import org.junit.Assert.*
import org.junit.Test

class IndonesianNormalizationTest {
    private fun prepare(text: String) = PreparedSpeech(text, IntArray(text.length) { it }).normalizedForNarration()

    @Test fun countsMillionsAndLocalDecimalDigits() {
        assertEquals("satu juta delapan ratus tujuh puluh empat ribu seratus enam puluh satu",
            prepare("1.874.161").text)
        assertEquals("satu miliar dua ratus tiga puluh empat juta lima ratus enam puluh tujuh ribu delapan ratus sembilan puluh",
            prepare("1.234.567.890").text)
        assertEquals("tiga koma nol lima persen", prepare("3,05%").text)
        assertEquals("minus dua koma nol satu", prepare("−2,01").text)
        assertEquals("dua belas ribu lima ratus koma nol lima rupiah", prepare("Rp12.500,05").text)
        assertEquals("seribu dua ratus lima puluh koma nol lima dolar Amerika", prepare("US$1,250.05").text)
    }

    @Test fun contextDistinguishesDatesTimesFractionsAndMath() {
        assertEquals("pukul tujuh lima", prepare("pukul 07.05").text)
        assertEquals("pukul tujuh tepat", prepare("07:00").text)
        assertEquals("dua puluh lima November dua ribu dua puluh enam", prepare("25/11/2026").text)
        assertEquals("dua puluh lima November dua ribu dua puluh enam", prepare("2026-11-25").text)
        assertEquals("tiga per empat", prepare("3/4").text)
        assertEquals("dua pangkat tiga tambah empat sama dengan dua belas", prepare("2³ + 4 = 12").text)
        assertEquals("lima sentimeter persegi", prepare("5 cm²").text)
        assertEquals("lima sampai sepuluh", prepare("5–10").text)
        assertEquals("Bab empat", prepare("Bab IV").text)
        assertEquals("MIX", prepare("MIX").text)
    }

    @Test fun leavesProtectedStringsAndSpellsPhonesAndCodes() {
        assertEquals("v1.3.23", prepare("v1.3.23").text)
        assertEquals("https://contoh.id/dr.12 a23@contoh.id", prepare("https://contoh.id/dr.12 a23@contoh.id").text)
        assertEquals("nol delapan satu dua tiga empat lima enam tujuh delapan sembilan nol", prepare("0812-3456-7890").text)
        assertEquals("ha dua o", prepare("H2O").text)
    }

    @Test fun ttsLimitDoesNotReduceAnOversizedExpansionToSingleLetters() {
        val expanded = prepare("1".repeat(150))
        val end = expanded.endWithin(0, 100)
        assertEquals(100, end)
        assertEquals("satu ".repeat(20), expanded.text.substring(0, end))
    }

    @Test fun expandedNumberRetainsWholeSourceSpanAndOriginalPass() {
        val source = "Ada 1.874.161 warga."
        val doc = ReadingDocument(listOf(Section("Bab", source)))
        assertEquals(1, doc.passes.size)
        val result = NarrationPipeline(doc, ListeningOptions()).prepare(0)
        val first = result.text.indexOf("satu")
        val last = result.text.indexOf(" warga") - 1
        assertEquals(4..12, result.sourceRange(first..last))
        assertEquals(first, result.startAt(8))
        assertEquals(source.indexOf("warga"), result.offsets[result.text.indexOf("warga")])
        assertTrue(ReadingTimeline(doc, ListeningOptions()).position(0, 0).totalWords > 8)
    }
}
