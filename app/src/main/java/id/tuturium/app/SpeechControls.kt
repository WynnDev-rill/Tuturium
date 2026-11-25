package id.tuturium.app

import android.content.Context
import java.util.Locale

/** These preferences only affect playback, never change stored books or their contents. */
data class ListeningOptions(
    val autoplay: Boolean = false,
    val hideAutoplay: Boolean = false,
    val autoScroll: Boolean = true,
    val matchLanguage: Boolean = true,
    val mixWithMusic: Boolean = false,
    val skipEnabled: Boolean = false,
    val skipHeaders: Boolean = true,
    val skipFooters: Boolean = true,
    val skipFootnotes: Boolean = true,
    val skipTables: Boolean = true,
    val skipFormulas: Boolean = true,
    val skipCitations: Boolean = true,
    val skipUrls: Boolean = true,
    val skipRound: Boolean = true,
    val skipSquare: Boolean = true,
    val skipCurly: Boolean = true,
    val autoRamp: Boolean = false,
    val sentencePauseMs: Int = 0,
    val paragraphPauseMs: Int = 0
) {
    companion object {
        fun read(context: Context): ListeningOptions =
            context.getSharedPreferences("listening_options", Context.MODE_PRIVATE).let { p ->
                ListeningOptions(
                    autoplay = p.getBoolean("autoplay", false),
                    hideAutoplay = p.getBoolean("hideAutoplay", false),
                    autoScroll = p.getBoolean("autoScroll", true),
                    matchLanguage = p.getBoolean("matchLanguage", true),
                    mixWithMusic = p.getBoolean("mixWithMusic", false),
                    skipEnabled = p.getBoolean("skipEnabled", false),
                    skipHeaders = p.getBoolean("skipHeaders", true),
                    skipFooters = p.getBoolean("skipFooters", true),
                    skipFootnotes = p.getBoolean("skipFootnotes", true),
                    skipTables = p.getBoolean("skipTables", true),
                    skipFormulas = p.getBoolean("skipFormulas", true),
                    skipCitations = p.getBoolean("skipCitations", true),
                    skipUrls = p.getBoolean("skipUrls", true),
                    skipRound = p.getBoolean("skipRound", true),
                    skipSquare = p.getBoolean("skipSquare", true),
                    skipCurly = p.getBoolean("skipCurly", true),
                    autoRamp = p.getBoolean("autoRamp", false),
                    sentencePauseMs = p.getInt("sentencePauseMs", 0).coerceIn(0, 1200),
                    paragraphPauseMs = p.getInt("paragraphPauseMs", 0).coerceIn(0, 1200)
                )
            }
    }
    fun save(context: Context) {
        context.getSharedPreferences("listening_options", Context.MODE_PRIVATE).edit().apply {
            putBoolean("autoplay", autoplay)
            putBoolean("hideAutoplay", hideAutoplay)
            putBoolean("autoScroll", autoScroll)
            putBoolean("matchLanguage", matchLanguage)
            putBoolean("mixWithMusic", mixWithMusic)
            putBoolean("skipEnabled", skipEnabled)
            putBoolean("skipHeaders", skipHeaders)
            putBoolean("skipFooters", skipFooters)
            putBoolean("skipFootnotes", skipFootnotes)
            putBoolean("skipTables", skipTables)
            putBoolean("skipFormulas", skipFormulas)
            putBoolean("skipCitations", skipCitations)
            putBoolean("skipUrls", skipUrls)
            putBoolean("skipRound", skipRound)
            putBoolean("skipSquare", skipSquare)
            putBoolean("skipCurly", skipCurly)
            putBoolean("autoRamp", autoRamp)
            putInt("sentencePauseMs", sentencePauseMs.coerceIn(0, 1200))
            putInt("paragraphPauseMs", paragraphPauseMs.coerceIn(0, 1200))
        }.apply()
    }
}

internal const val AUTO_SPEED_WORDS = 600
internal const val SPEED_STEP = 0.05f
internal val PLAYBACK_SPEED_RANGE = 0.5f..4f

internal fun rampedSpeed(base: Float, listenedWords: Int, enabled: Boolean): Float {
    val bounded = base.coerceIn(PLAYBACK_SPEED_RANGE)
    if (!enabled) return bounded
    val steps = listenedWords.coerceAtLeast(0) / AUTO_SPEED_WORDS
    return (bounded + steps * SPEED_STEP).coerceAtMost(PLAYBACK_SPEED_RANGE.endInclusive)
}

/** Counts completed speech within the current document and base setting. */
internal class AutomaticSpeed {
    private var base = 1f
    private var enabled = false
    private var completedWords = 0

    val rate: Float get() = rampedSpeed(base, completedWords, enabled)

    fun configure(speed: Float, ramp: Boolean) {
        if (speed != base || ramp != enabled) {
            base = speed
            enabled = ramp
            reset()
        }
    }

    fun spoken(words: Int) {
        if (enabled && words > 0 && rate < PLAYBACK_SPEED_RANGE.endInclusive) {
            val limit = 70 * AUTO_SPEED_WORDS // 0.5× to 4× in 0.05× steps.
            completedWords += words.coerceAtMost(limit - completedWords)
        }
    }

    fun reset() { completedWords = 0 }
}

/** 10 seconds is an approximate word-based seek; Android TTS does not expose exact PCM timeline. */
fun seekBySeconds(passes: List<Pass>, current: Int, seconds: Int, speed: Float): Int {
    if (passes.isEmpty()) return 0
    val deltaWords = (seconds * 150f * speed.coerceIn(0.5f, 4f) / 60f).toInt()
    if (deltaWords == 0) return current.coerceIn(0, passes.lastIndex)
    var words = 0
    var idx = current.coerceIn(0, passes.lastIndex)
    if (deltaWords > 0) {
        while (idx < passes.lastIndex && words < deltaWords) {
            words += countDocumentWords(passes[idx].text)
            idx++
        }
    } else {
        while (idx > 0 && words < -deltaWords) {
            idx--
            words += countDocumentWords(passes[idx].text)
        }
    }
    return idx
}

/** A mapping for word highlighting after skip filters remove text. */
data class PreparedSpeech(val text: String, val offsets: IntArray, val ends: IntArray = offsets) {
    internal fun startAt(source: Int): Int = ends.indexOfFirst { it >= source }
    internal fun sourceRange(range: IntRange): IntRange? {
        val start = offsets.getOrNull(range.first) ?: return null
        val end = ends.getOrNull(range.last) ?: return null
        return start..end
    }
    internal fun endWithin(start: Int, maxCharacters: Int): Int {
        val limit = minOf(start + maxCharacters, text.length)
        if (limit == text.length) return limit
        // Keep normal expansions together. An identifier can expand beyond the entire engine limit.
        val candidates = limit downTo start + 1
        val tokenBoundary = candidates.firstOrNull {
            text[it - 1].isWhitespace() && offsets[it - 1] != offsets[it]
        }
        var end = tokenBoundary ?: candidates.firstOrNull { text[it - 1].isWhitespace() } ?: limit
        if (text[end].isLowSurrogate() && text[end - 1].isHighSurrogate() && end > start + 1) end--
        return end
    }

    internal fun from(start: Int) = PreparedSpeech(text.substring(start),
        offsets.copyOfRange(start, offsets.size), ends.copyOfRange(start, ends.size))
}

private val footerPattern = Regex("""(?im)^[ \t]*(?:halaman|page)[ \t]+\d+(?:[ \t]*(?:/|dari|of)[ \t]*\d+)?[ \t]*$""")
private val footnotePattern = Regex("""(?m)^[ \t]*(?:\[\d{1,3}\]|[⁰¹²³⁴⁵⁶⁷⁸⁹]+)[ \t]+[^\n]+$""")
private val tablePattern = Regex("""(?m)^[ \t]*(?:\|[^|\n]+\|[^|\n]+\|[^\n]*|[^\t\n]+\t[^\t\n]+\t[^\n]+)$""")
private val formulaPattern = Regex("""(?m)^[ \t]*[A-Za-zα-ωΑ-Ω][A-Za-z0-9_]*[ \t]*=[ \t]*[^\n]+$""")
private val citationPattern = Regex("""\([^()\r\n]{2,80},[ \t]*(?:19|20)\d\d(?:[ \t]*:[ \t]*\d+)?\)""")
private val urlPattern = Regex("""(?i)(?:https?://|www\.)[^\s<>()]+""")
private val roundPattern = Regex("""\([^()\n]{1,200}\)""")
private val squarePattern = Regex("""\[[^\[\]\n]{1,200}\]""")
private val curlyPattern = Regex("""\{[^{}\n]{1,200}\}""")

fun prepareSpeech(source: String, settings: ListeningOptions): PreparedSpeech {
    if (!settings.skipEnabled) return PreparedSpeech(source, IntArray(source.length) { it })
    val blocked = BooleanArray(source.length)
    fun block(enabled: Boolean, pattern: Regex) {
        if (enabled) pattern.findAll(source).forEach { match ->
            java.util.Arrays.fill(blocked, match.range.first, match.range.last + 1, true)
        }
    }
    // Chapter headings belong to the book. Repeated headers are identified from PDF layout.
    block(settings.skipFooters, footerPattern)
    block(settings.skipFootnotes, footnotePattern)
    block(settings.skipTables, tablePattern)
    block(settings.skipFormulas, formulaPattern)
    block(settings.skipCitations, citationPattern)
    block(settings.skipUrls, urlPattern)
    block(settings.skipRound, roundPattern)
    block(settings.skipSquare, squarePattern)
    block(settings.skipCurly, curlyPattern)

    val result = StringBuilder(source.length)
    val offsets = IntArray(source.length)
    for (index in source.indices) {
        if (!blocked[index] && (result.isNotEmpty() || !source[index].isWhitespace())) {
            offsets[result.length] = index
            result.append(source[index])
        }
    }
    while (result.isNotEmpty() && result.last().isWhitespace()) result.setLength(result.length - 1)
    return PreparedSpeech(result.toString(), offsets.copyOf(result.length))
}

/** Keep document coordinates stable even when a filter spans several TTS passes. */
internal fun PreparedSpeech.forPass(pass: Pass): PreparedSpeech {
    fun atOrAfter(position: Int) = offsets.binarySearch(position).let { if (it < 0) -it - 1 else it }
    var first = atOrAfter(pass.offset)
    var until = atOrAfter(pass.offset + pass.text.length)
    while (first < until && text[first].isWhitespace()) first++
    while (until > first && text[until - 1].isWhitespace()) until--
    return PreparedSpeech(text.substring(first, until), IntArray(until - first) { offsets[first + it] - pass.offset })
}

/** A voice that works in the 1× preview may reject a custom speaking rate.
 *  Only retry recoverable synthesis failures once; never loop on network errors. */
fun shouldRetrySpeechAtNormalSpeed(speed: Float, retried: Boolean, recoverable: Boolean): Boolean =
    recoverable && !retried && kotlin.math.abs(speed - 1f) > 0.01f

/** A device-specific filter failure must not prevent a book from being read.
 * Every fallback offset maps directly to the original paragraph. */
fun safePreparedSpeech(
    source: String,
    settings: ListeningOptions,
    prepare: (String, ListeningOptions) -> PreparedSpeech = ::prepareSpeech
): PreparedSpeech {
    fun original() = PreparedSpeech(source, IntArray(source.length) { it })
    return try {
        val prepared = prepare(source, settings)
        if (prepared.offsets.size != prepared.text.length || prepared.ends.size != prepared.text.length ||
            prepared.offsets.any { it !in source.indices }) original()
        else prepared
    } catch (_: Exception) {
        original()
    }
}
