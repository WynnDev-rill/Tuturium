package id.tuturium.app

import android.content.Context
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round

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
    val autoRamp: Boolean = false
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
                    autoRamp = p.getBoolean("autoRamp", false)
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
        }.apply()
        if (ReadingService.state.value.bookId != null)
            ReadingService.command(context, ReadingService.SETTINGS)
    }
}

/** The user-selected speed is the starting point, not a hard 30 fps audio dependency. */
fun rampedSpeed(base: Float, listenedWords: Int, enabled: Boolean): Float {
    val bounded = base.coerceIn(0.5f, 4f)
    if (!enabled) return bounded
    return ((round(bounded * 10f) / 10f) +
        floor(listenedWords.coerceAtLeast(0) / 500f) * 0.1f).coerceAtMost(4f)
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
data class PreparedSpeech(val text: String, val offsets: IntArray)

fun prepareSpeech(source: String, settings: ListeningOptions): PreparedSpeech {
    if (!settings.skipEnabled) return PreparedSpeech(source, IntArray(source.length) { it })
    val blocked = BooleanArray(source.length)
    fun block(match: MatchResult) {
        for (i in match.range) if (i in blocked.indices) blocked[i] = true
    }
    fun patterns(regex: Regex) { regex.findAll(source).forEach(::block) }
    // Only very distinctive entire-line structures are skipped.
    if (settings.skipHeaders) patterns(Regex(
        """(?im)^\s*(?:bab|chapter|daftar isi|pendahuluan)\s+[0-9ivxlcdm]+\b[^\n]*$"""))
    if (settings.skipFooters) patterns(Regex("""(?im)^\s*(?:halaman|page)\s+\d+\s*$"""))
    if (settings.skipFootnotes) patterns(Regex("""(?m)^\s*(?:\[\d{1,3}]|\d{1,3}[.)])\s+.{6,100}$"""))
    if (settings.skipTables) patterns(Regex("""(?m)^\s*\|[^|\n]+\|[^|\n]+\|[^\n]*$"""))
    if (settings.skipFormulas) patterns(Regex("""(?m)^\s*[A-Za-zα-ωΑ-Ω][A-Za-z0-9_]*\s*=\s*[^\n]+$"""))
    if (settings.skipCitations) patterns(Regex("""\((?:[^()]|\s){2,45},\s*(?:19|20)\d\d(?:\s*:\s*\d+)?\)"""))
    if (settings.skipUrls) patterns(Regex("""(?i)(?:https?://|www\.)[^\s<>()]+"""))
    if (settings.skipRound) patterns(Regex("""\([^()\n]{1,200}\)"""))
    if (settings.skipSquare) patterns(Regex("""\[[^\[\]\n]{1,200}]"""))
    if (settings.skipCurly) patterns(Regex("""\{[^{}\n]{1,200}}"""))
    val result = StringBuilder()
    val indices = ArrayList<Int>()
    for (i in source.indices) {
        if (!blocked[i]) {
            result.append(source[i])
            indices.add(i)
        }
    }
    val first = result.indexOfFirst { !it.isWhitespace() }
    if (first < 0) return PreparedSpeech("", IntArray(0))
    val last = result.indexOfLast { !it.isWhitespace() }
    return PreparedSpeech(result.substring(first, last + 1),
        indices.subList(first, last + 1).toIntArray())
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
        if (prepared.offsets.size != prepared.text.length ||
            prepared.offsets.any { it !in source.indices }) original()
        else prepared
    } catch (_: Exception) {
        original()
    }
}
