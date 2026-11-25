package id.tuturium.app

import android.content.Context
import android.util.AtomicFile
import android.util.LruCache
import java.io.DataInputStream
import java.io.DataOutputStream
import java.security.MessageDigest
import java.io.File
import java.util.Locale
import kotlin.math.abs
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject

/** Fractions of the unzoomed, rotated PDF page. The stored box describes the included area. */
internal data class PdfMargins(val left: Float = 0f, val top: Float = 0f,
    val right: Float = 1f, val bottom: Float = 1f) {
    fun includes(word: PdfWordHit) =
        (word.left + word.right) / 2 in left..right && (word.top + word.bottom) / 2 in top..bottom

    fun move(edge: Int, fraction: Float): PdfMargins = when (edge) {
        0 -> copy(left = fraction.coerceIn(0f, right - 0.10f))
        1 -> copy(top = fraction.coerceIn(0f, bottom - 0.10f))
        2 -> copy(right = fraction.coerceIn(left + 0.10f, 1f))
        else -> copy(bottom = fraction.coerceIn(top + 0.10f, 1f))
    }

    fun json() = JSONArray(listOf(left, top, right, bottom))

    companion object {
        fun read(array: JSONArray): PdfMargins {
            val values = List(4) { array.getDouble(it).toFloat() }
            require(values.all { it in 0f..1f })
            require(values[2] - values[0] >= .099f && values[3] - values[1] >= .099f)
            return PdfMargins(values[0], values[1], values[2], values[3])
        }
    }
}

internal data class PdfMarginSettings(val all: PdfMargins = PdfMargins(),
    val pages: Map<Int, PdfMargins> = emptyMap()) {
    fun page(index: Int) = pages[index] ?: all
    val empty get() = all == PdfMargins() && pages.isEmpty()
}

internal object PdfMarginStore {
    private fun prefs(context: Context) = context.getSharedPreferences("pdf_reading_margins", Context.MODE_PRIVATE)

    fun read(context: Context, id: String): PdfMarginSettings = runCatching {
        val raw = prefs(context).getString(id, null) ?: return PdfMarginSettings()
        val value = JSONObject(raw)
        val pages = value.getJSONObject("pages")
        PdfMarginSettings(PdfMargins.read(value.getJSONArray("all")), buildMap {
            pages.keys().forEach { key -> put(key.toInt(), PdfMargins.read(pages.getJSONArray(key))) }
        })
    }.getOrDefault(PdfMarginSettings())

    fun write(context: Context, id: String, settings: PdfMarginSettings) {
        val editor = prefs(context).edit()
        if (settings.empty) editor.remove(id)
        else editor.putString(id, JSONObject().put("all", settings.all.json()).put("pages", JSONObject().apply {
            settings.pages.forEach { (page, area) -> put(page.toString(), area.json()) }
        }).toString())
        editor.apply()
    }
}

internal enum class PdfContentKind { MARGIN, HEADER, FOOTER, FOOTNOTE, TABLE, DECORATION }

internal data class PdfContentSpan(val range: IntRange, val kind: PdfContentKind)

internal class PdfNarrationPlan(private val spans: Map<Int, List<PdfContentSpan>>) {
    fun excluded(index: Int, options: ListeningOptions): List<IntRange> {
        val ranges = spans[index].orEmpty().asSequence().filter { span ->
            span.kind == PdfContentKind.MARGIN || options.skipEnabled && when (span.kind) {
                PdfContentKind.HEADER -> options.skipHeaders
                PdfContentKind.FOOTER -> options.skipFooters
                PdfContentKind.FOOTNOTE -> options.skipFootnotes
                PdfContentKind.TABLE -> options.skipTables
                PdfContentKind.DECORATION -> options.skipHeaders || options.skipFooters
                PdfContentKind.MARGIN -> true
            }
        }.map { it.range }.sortedBy { it.first }
        return buildList {
            for (range in ranges) {
                val previous = lastOrNull()
                if (previous != null && range.first <= previous.last + 1)
                    set(lastIndex, previous.first..maxOf(previous.last, range.last))
                else add(range)
            }
        }
    }

    fun write(output: DataOutputStream) {
        output.writeInt(spans.size)
        for ((pass, ranges) in spans) {
            output.writeInt(pass)
            output.writeInt(ranges.size)
            for (span in ranges) {
                output.writeInt(span.range.first)
                output.writeInt(span.range.last)
                output.writeByte(span.kind.ordinal)
            }
        }
    }

    companion object {
        val EMPTY = PdfNarrationPlan(emptyMap())

        fun read(input: DataInputStream, document: ReadingDocument): PdfNarrationPlan {
            val count = input.readInt().also { require(it in 0..document.passes.size) }
            return PdfNarrationPlan(buildMap {
                repeat(count) {
                    val index = input.readInt().also { require(it in document.passes.indices) }
                    val length = document.passes[index].text.length
                    val size = input.readInt().also { require(it in 0..length * 2) }
                    put(index, List(size) {
                        val start = input.readInt()
                        val end = input.readInt()
                        require(start in 0 until length && end in start until length)
                        PdfContentSpan(start..end, PdfContentKind.entries[input.readUnsignedByte()])
                    })
                }
            })
        }
    }
}

private data class PdfLine(val page: Int, val words: List<PdfWordHit>) {
    val text = words.joinToString(" ") { it.text }
    val center = words.map { (it.top + it.bottom) / 2f }.average().toFloat()
    val height = words.map { it.fontScale ?: (it.bottom - it.top) }.sorted()[words.size / 2]
    val band = when {
        center < .095f -> 1
        center > .915f -> 2
        else -> 0
    }
    val number = text.trim().removePrefix("Halaman ").removePrefix("Page ").toIntOrNull()
    val repeatedText = text.lowercase(Locale.ROOT).replace(numberAtEnd, "").trim()
    val chapter = chapterHeading.containsMatchIn(text)
}

private val numberAtEnd = Regex("""\s+\d+\s*$""")
private val chapterHeading = Regex("""(?i)^(bab|chapter|bagian|jilid)\b""")
private val decorativeText = setOf("draf", "draft", "contoh", "sample", "pratinjau")
private val footnoteLabel = Regex("""^(?:\d{1,2}|[⁰¹²³⁴⁵⁶⁷⁸⁹]+)\s+\p{L}""")

private fun layoutLines(page: Int, words: List<PdfWordHit>): List<PdfLine> {
    val rows = mutableListOf<MutableList<PdfWordHit>>()
    for (word in words.sortedBy { it.bottom }) {
        if (word.left >= word.right || word.top >= word.bottom) continue
        val line = rows.lastOrNull()
        if (line == null || abs(line.first().bottom - word.bottom) > .004f) rows.add(mutableListOf(word))
        else line.add(word)
    }
    return rows.map { PdfLine(page, it.sortedBy(PdfWordHit::left)) }
}

private fun PdfLine.cells(): List<Float> {
    if (words.size !in 3..12) return emptyList()
    return listOf(words.first().left) + words.zipWithNext().mapNotNull { (a, b) ->
        b.left.takeIf { it - a.right > .06f }
    }
}

/** Require corroboration across pages before suppressing running text or bare page numbers. */
internal fun buildPdfNarrationPlan(document: ReadingDocument, pages: Map<Int, List<PdfWordHit>>,
    margins: PdfMarginSettings, detect: Boolean): PdfNarrationPlan {
    val lines = if (detect) pages.flatMap { (page, words) -> layoutLines(page, words) } else emptyList()
    val kinds = mutableMapOf<PdfLine, PdfContentKind>()
    val bodyHeights = lines.filter { it.band == 0 && it.words.size >= 5 }.map { it.height }.sorted()
    val bodyHeight = bodyHeights.getOrNull(bodyHeights.size / 2)

    lines.filter { it.band != 0 && it.number == null && it.words.size <= 16 && it.repeatedText.length >= 3 }
        .groupBy { Triple(it.band, (it.center * 40).toInt(), it.repeatedText) }.values.forEach { repeated ->
            if (repeated.map { it.page }.distinct().size < 3) return@forEach
            val first = repeated.minBy { it.page }
            for (line in repeated) {
                if (line == first && line.chapter) continue
                if (bodyHeight != null && line.height > bodyHeight * 1.25f) continue
                kinds[line] = when {
                    line.repeatedText in decorativeText && (line.center < .05f || line.center > .965f) ->
                        PdfContentKind.DECORATION
                    line.band == 1 -> PdfContentKind.HEADER
                    else -> PdfContentKind.FOOTER
                }
            }
        }
    lines.filter { it.band != 0 && it.number != null }.groupBy {
        it.band to (checkNotNull(it.number) - it.page)
    }.values.forEach { sequence ->
        if (sequence.map { it.page }.distinct().size >= 3)
            sequence.forEach { kinds[it] = if (it.band == 1) PdfContentKind.HEADER else PdfContentKind.FOOTER }
    }
    if (bodyHeight != null) lines.filter {
        it.center in 0.70f..0.96f && it.height < bodyHeight * .8f && footnoteLabel.containsMatchIn(it.text)
    }.forEach { kinds.putIfAbsent(it, PdfContentKind.FOOTNOTE) }

    lines.groupBy { it.page }.values.forEach { pageLines ->
        for (triple in pageLines.windowed(3)) {
            val first = triple.first().cells()
            if (first.size < 3 || triple.last().center - triple.first().center > .14f) continue
            if (triple.drop(1).all { row ->
                val cells = row.cells()
                cells.size == first.size && cells.zip(first).all { (a, b) -> abs(a - b) <= .025f }
            }) triple.forEach { kinds.putIfAbsent(it, PdfContentKind.TABLE) }
        }
    }

    val spans = mutableMapOf<Int, MutableList<PdfContentSpan>>()
    fun add(word: PdfWordHit, kind: PdfContentKind) {
        val (index, offset) = word.selection ?: return
        val pass = document.passes.getOrNull(index) ?: return
        if (offset !in pass.text.indices) return
        var until = (offset + word.text.length).coerceAtMost(pass.text.length)
        while (until < pass.text.length && pass.text[until] in ",.;:!?%”’)]}") until++
        spans.getOrPut(index) { mutableListOf() }.add(PdfContentSpan(offset until until, kind))
    }
    kinds.forEach { (line, kind) -> line.words.forEach { add(it, kind) } }
    for ((page, words) in pages) {
        val area = margins.page(page)
        if (area != PdfMargins()) words.filterNot(area::includes).forEach { add(it, PdfContentKind.MARGIN) }
    }
    return if (spans.isEmpty()) PdfNarrationPlan.EMPTY else PdfNarrationPlan(spans)
}

internal object PdfNarrationStore {
    private data class Key(val source: String, val changedAt: Long, val textRevision: Long,
        val margins: PdfMarginSettings, val detect: Boolean)
    private val cache = LruCache<Key, PdfNarrationPlan>(4)
    private val locks = Array(8) { Mutex() }
    private const val PLAN_LIMIT = 8L * 1024 * 1024
    private const val CACHE_LIMIT = 24L * 1024 * 1024

    private fun cachedFile(context: Context, key: Key): AtomicFile {
        val root = File(context.cacheDir, "pdf-narration").apply { mkdirs() }
        val name = MessageDigest.getInstance("SHA-256").digest("layout-v1:$key".toByteArray())
            .joinToString("") { "%02x".format(it.toInt() and 255) }
        return AtomicFile(File(root, name))
    }

    private fun loadCached(file: AtomicFile, document: ReadingDocument): PdfNarrationPlan? =
        if (file.baseFile.length() !in 4L..PLAN_LIMIT) null else runCatching {
            DataInputStream(file.openRead().buffered()).use { PdfNarrationPlan.read(it, document) }
                .also { file.baseFile.setLastModified(System.currentTimeMillis()) }
        }.getOrNull()

    @Synchronized
    private fun saveCached(file: AtomicFile, plan: PdfNarrationPlan) {
        runCatching {
            val stream = file.startWrite()
            try {
                val data = DataOutputStream(stream.buffered())
                plan.write(data)
                data.flush()
                file.finishWrite(stream)
            } catch (error: Exception) {
                file.failWrite(stream)
                throw error
            }
            if (file.baseFile.length() > PLAN_LIMIT) file.delete()
            val files = file.baseFile.parentFile?.listFiles().orEmpty().sortedBy(File::lastModified)
            var total = files.sumOf(File::length)
            for (entry in files) {
                if (total <= CACHE_LIMIT) break
                val bytes = entry.length()
                if (entry.delete()) total -= bytes
            }
        }
    }

    suspend fun load(context: Context, source: File, textRevision: Long, document: ReadingDocument,
        margins: PdfMarginSettings, detect: Boolean): PdfNarrationPlan = withContext(Dispatchers.IO) {
        val key = Key(source.path, source.lastModified(), textRevision, margins, detect)
        locks[key.hashCode().and(Int.MAX_VALUE) % locks.size].withLock {
            cache.get(key)?.let { return@withLock it }
            val file = cachedFile(context, key)
            val restored = loadCached(file, document)
            if (restored != null) {
                cache.put(key, restored)
                return@withLock restored
            }
            build(context, source, document, margins, detect).also {
                cache.put(key, it)
                saveCached(file, it)
            }
        }
    }

    private suspend fun build(context: Context, source: File, document: ReadingDocument,
        margins: PdfMarginSettings, detect: Boolean): PdfNarrationPlan {
        val job = coroutineContext
        val pagePasses = document.passes.withIndex().groupBy { document.pdfPageForPass(it.index) }
        val pages = mutableMapOf<Int, List<PdfWordHit>>()
        fun put(page: Int, hits: List<PdfWordHit>) {
            // A mismatched text layer cannot authorize suppressing original imported text.
            if (hits.isNotEmpty() && hits.count { it.selection != null } * 100L >= hits.size * 80L) {
                pages[page] = hits
            }
        }
        try {
            val neededPages = pagePasses.keys.filter { page -> detect || margins.page(page) != PdfMargins() }
            if (neededPages.isEmpty()) return PdfNarrationPlan.EMPTY
            readPdfWords(context, source, neededPages.min(), neededPages.max()) { page, words ->
                job.ensureActive()
                if (detect || margins.page(page) != PdfMargins())
                    put(page, alignPdfHits(words, pagePasses[page].orEmpty()))
            }
            val scans = (pagePasses.keys - pages.keys).filter { page ->
                detect || margins.page(page) != PdfMargins()
            }.sorted()
            if (scans.isNotEmpty()) {
                val renderer = PdfPageSession(source)
                try {
                    for (page in scans) {
                        job.ensureActive()
                        try {
                            put(page, ocrPdfWordHits(renderer.render(page, 1100), pagePasses.getValue(page)))
                        } catch (error: CancellationException) {
                            throw error
                        } catch (error: Exception) {
                            android.util.Log.w("TuturiumPdf", "OCR layout unavailable", error)
                        }
                    }
                } finally { renderer.close() }
            }
            return buildPdfNarrationPlan(document, pages, margins, detect)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            android.util.Log.w("TuturiumPdf", "PDF layout unavailable", error)
            return PdfNarrationPlan.EMPTY
        }
    }
}
