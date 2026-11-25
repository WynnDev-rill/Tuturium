package id.tuturium.app

/** Per-reader cursor. Stored pass boundaries always come from the original document. */
internal class NarrationPipeline(private val document: ReadingDocument, private val options: ListeningOptions) {
    private val semanticOptions = if (document.hasPdfTextGeometry)
        options.copy(skipFooters = false, skipFootnotes = false) else options
    private var paragraph: Pair<Int, Int>? = null
    private var prepared = PreparedSpeech("", IntArray(0))

    fun prepare(index: Int): PreparedSpeech {
        val pass = document.passes[index]
        val speech = if (!options.skipEnabled) {
            PreparedSpeech(pass.text, IntArray(pass.text.length) { it })
        } else {
            val location = pass.section to pass.paragraph
            if (paragraph != location) {
                paragraph = location
                prepared = safePreparedSpeech(document.paragraphs[pass.section][pass.paragraph], semanticOptions)
            }
            prepared.forPass(pass)
        }
        val excluded = document.narrationLayout.excluded(index, options)
        return speech.excluding(excluded).normalizedForNarration()
    }
}

private fun PreparedSpeech.excluding(ranges: List<IntRange>): PreparedSpeech {
    if (ranges.isEmpty() || text.isEmpty()) return this
    val display = StringBuilder(text.length)
    val kept = IntArray(text.length)
    var rangeIndex = 0
    for (i in text.indices) {
        val location = offsets[i]
        while (rangeIndex < ranges.size && ranges[rangeIndex].last < location) rangeIndex++
        if (rangeIndex < ranges.size && location in ranges[rangeIndex]) continue
        if (display.isEmpty() && text[i].isWhitespace()) continue
        kept[display.length] = location
        display.append(text[i])
    }
    val result = display.toString().trimEnd()
    if (result.none(Char::isLetterOrDigit)) return PreparedSpeech("", IntArray(0))
    return PreparedSpeech(result, kept.copyOf(result.length))
}
