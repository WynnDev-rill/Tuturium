package id.tuturium.app

import kotlin.math.ceil

internal const val INITIAL_MILLIS_PER_WORD = 400.0

/** Document position uses source words. Time uses only the words sent to the engine. */
internal class ReadingTimeline(document: ReadingDocument, options: ListeningOptions) {
    private val sourceEnds = Array(document.passes.size) { index -> documentWordEnds(document.passes[index].text) }
    private val spokenEnds = if (options.skipEnabled) Array(sourceEnds.size) { IntArray(0) } else sourceEnds
    private val sourceWords = IntArray(sourceEnds.size + 1)
    private val spokenWords = IntArray(sourceEnds.size + 1)
    private val extraPauses = LongArray(sourceEnds.size + 1)

    init {
        var paragraph: Pair<Int, Int>? = null
        var prepared = PreparedSpeech("", IntArray(0))
        document.passes.forEachIndexed { index, pass ->
            if (options.skipEnabled) {
                val location = pass.section to pass.paragraph
                if (location != paragraph) {
                    paragraph = location
                    prepared = safePreparedSpeech(document.paragraphs[pass.section][pass.paragraph], options)
                }
                val filtered = prepared.forPass(pass)
                spokenEnds[index] = documentWordEnds(filtered.text).apply {
                    for (word in indices) this[word] = filtered.offsets[this[word] - 1] + 1
                }
            }
            sourceWords[index + 1] = sourceWords[index] + sourceEnds[index].size
            spokenWords[index + 1] = spokenWords[index] + spokenEnds[index].size
            extraPauses[index + 1] = extraPauses[index] +
                if (spokenEnds[index].isNotEmpty() &&
                    (options.sentencePauseMs > 0 || options.paragraphPauseMs > 0)) {
                    document.pauseAfter(index, options).extraMs
                } else 0
        }
    }

    // The arrays encode positions per word, without retaining the document's text in the cache.
    val sizeBytes: Int get() = 1 + sourceWords.last() * 4 +
        (if (spokenEnds === sourceEnds) 0 else spokenWords.last() * 4) + (sourceWords.size * 48)

    fun position(index: Int, offset: Int): ReadingPosition {
        val pass = index.coerceIn(0, sourceEnds.size)
        val read = sourceWords[pass] + (sourceEnds.getOrNull(pass)?.wordsBefore(offset) ?: 0)
        val spoken = spokenWords[pass] + (spokenEnds.getOrNull(pass)?.wordsBefore(offset) ?: 0)
        if (pass == sourceEnds.size && pass > 0) return ReadingPosition(1f, 0)
        val progress = (read.toFloat() / sourceWords.last().coerceAtLeast(1)).coerceAtMost(0.999999f)
        return ReadingPosition(progress, spokenWords.last() - spoken, extraPauses.last() - extraPauses[pass])
    }

    private fun IntArray.wordsBefore(offset: Int): Int {
        // Several mapped words can end at the same source character.
        var low = 0
        var high = size
        while (low < high) {
            val middle = (low + high) ushr 1
            if (this[middle] <= offset) low = middle + 1 else high = middle
        }
        return low
    }
}

internal data class ReadingPosition(
    val fraction: Float,
    val remainingWords: Int,
    val remainingPauseMs: Long = 0
) {
    val percent: Int get() = (fraction * 100).toInt().coerceIn(0, 100)

    fun remainingSeconds(millisPerWord: Double, speed: Float): Int =
        ceil((remainingWords * millisPerWord + remainingPauseMs) /
            (1_000.0 * speed.coerceIn(PLAYBACK_SPEED_RANGE))).toInt()

    fun remainingMinutes(millisPerWord: Double, speed: Float): Int =
        ceil(remainingSeconds(millisPerWord, speed) / 60.0).toInt()
}

/** Strip fields that have no effect on the document's narration plan. */
internal fun ListeningOptions.timelineOptions(): ListeningOptions = copy(
    autoplay = false, hideAutoplay = false, autoScroll = true, matchLanguage = true,
    mixWithMusic = false, autoRamp = false
)
