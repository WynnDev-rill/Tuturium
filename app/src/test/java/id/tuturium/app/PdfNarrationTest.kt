package id.tuturium.app

import org.junit.Assert.*
import org.junit.Test

class PdfNarrationTest {
    @Test fun repeatedRunningTitlesAndSequentialEdgeNumbersNeedCrossPageEvidence() {
        val docs = (1..4).map { page ->
            Section("Halaman $page", "Bab IV\nAngka $page adalah hasil penelitian yang dibaca.\n$page")
        }
        val document = ReadingDocument(docs)
        val pages = docs.indices.associateWith { page ->
            val lines = listOf("Bab IV" to .05f,
                "Angka ${page + 1} adalah hasil penelitian yang dibaca" to .4f, "${page + 1}" to .97f)
            val words = lines.flatMap { (line, top) ->
                line.split(' ').mapIndexed { index, word ->
                    PdfWordHit(word, .1f + index * .09f, top, .16f + index * .09f, top + .01f)
                }
            }
            alignPdfHits(words, document.passes.withIndex().filter { it.value.section == page })
        }
        document.narrationLayout = buildPdfNarrationPlan(document, pages, PdfMarginSettings(), detect = true)
        val filtered = NarrationPipeline(document, ListeningOptions(skipEnabled = true))
        val raw = NarrationPipeline(document, ListeningOptions())
        val positions = document.passes.withIndex().groupBy { it.value.section }
        assertTrue(filtered.prepare(positions.getValue(0).first().index).text.contains("Bab empat"))
        for (page in 1..3) {
            val passes = positions.getValue(page)
            assertTrue(filtered.prepare(passes.first().index).text.isBlank())
            assertTrue(filtered.prepare(passes[1].index).text.contains("adalah hasil penelitian"))
            assertTrue(filtered.prepare(passes.last().index).text.isBlank())
            assertFalse(raw.prepare(passes.last().index).text.isBlank())
        }
        document.narrationLayout = buildPdfNarrationPlan(document, pages.filterKeys { it == 0 },
            PdfMarginSettings(), detect = true)
        assertFalse(filtered.prepare(positions.getValue(0).last().index).text.isBlank())
    }

    @Test fun distinctArabicNumberedChapterHeadingsRemainAudible() {
        val doc = ReadingDocument((1..4).map { Section("Halaman $it", "Bab $it") })
        val pages = doc.passes.indices.associateWith { page ->
            alignPdfHits(listOf(PdfWordHit("Bab", .10f, .05f, .18f, .065f),
                PdfWordHit("${page + 1}", .20f, .05f, .23f, .065f)),
                listOf(IndexedValue(page, doc.passes[page])))
        }
        doc.narrationLayout = buildPdfNarrationPlan(doc, pages, PdfMarginSettings(), detect = true)
        val pipeline = NarrationPipeline(doc, ListeningOptions(skipEnabled = true))
        assertEquals(listOf("Bab satu", "Bab dua", "Bab tiga", "Bab empat"), doc.passes.indices.map(pipeline::prepare).map { it.text })
    }

    @Test fun pageOverridesAndRelativeMarginsWorkWithoutAutomaticSkip() {
        val text = "Pinggir. Isi utama dibaca."
        val doc = ReadingDocument(listOf(Section("Halaman 1", text), Section("Halaman 2", text)))
        val pages = (0..1).associateWith { page ->
            alignPdfHits(listOf(PdfWordHit("Pinggir", .04f, .04f, .16f, .07f),
                PdfWordHit("Isi", .25f, .30f, .29f, .33f),
                PdfWordHit("utama", .30f, .30f, .40f, .33f),
                PdfWordHit("dibaca", .42f, .30f, .52f, .33f)),
                doc.passes.withIndex().filter { it.value.section == page })
        }
        doc.narrationLayout = buildPdfNarrationPlan(doc, pages,
            PdfMarginSettings(PdfMargins(top = .2f), mapOf(1 to PdfMargins())), detect = false)
        val pipeline = NarrationPipeline(doc, ListeningOptions())
        assertEquals("", pipeline.prepare(0).text)
        assertEquals("Isi utama dibaca.", pipeline.prepare(1).text)
        assertEquals("Pinggir.", pipeline.prepare(2).text)
        assertEquals(2, doc.firstPassByPdfPage().getValue(1))
    }
}
