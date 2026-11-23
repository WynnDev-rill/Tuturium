package id.tuturium.app

import java.text.BreakIterator
import java.util.Locale

data class Section(val title: String, val text: String)
data class Pass(val text: String, val section: Int, val paragraph: Int, val offset: Int)

data class ReadingDocument(val sections: List<Section>) {
    val passes: List<Pass> = sections.flatMapIndexed { section, content ->
        content.text.split(Regex("\\n+")).filter { it.isNotBlank() }
            .flatMapIndexed { paragraph, text -> splitSpeech(text, section, paragraph) }
    }
}

fun splitSpeech(text: String, section: Int = 0, paragraph: Int = 0, limit: Int = 1200): List<Pass> {
    require(limit >= 2)
    val iterator = BreakIterator.getSentenceInstance(Locale.forLanguageTag("id-ID"))
    iterator.setText(text)
    val passes = mutableListOf<Pass>()
    var beginning = iterator.first()
    var ending = iterator.next()
    while (ending != BreakIterator.DONE) {
        while (beginning < ending) {
            while (beginning < ending && text[beginning].isWhitespace()) beginning++
            if (beginning >= ending) break
            var cut = minOf(beginning + limit, ending)
            if (cut < ending) {
                val space = text.lastIndexOf(' ', cut)
                if (space > beginning) cut = space
                if (text[cut].isLowSurrogate() && text[cut - 1].isHighSurrogate()) cut--
            }
            val phrase = text.substring(beginning, cut).trimEnd()
            if (phrase.isNotEmpty()) passes += Pass(phrase, section, paragraph, beginning)
            beginning = cut
        }
        ending = iterator.next()
    }
    return passes
}

data class VoiceChoice(val name: String, val language: String, val label: String, val quality: Int)

internal fun isIndonesian(language: String) = language == "id" || language == "in" || language == "ind"

fun chooseVoice(voices: List<VoiceChoice>, selected: String?, systemLanguage: String): VoiceChoice? =
    voices.firstOrNull { it.name == selected } ?: voices.sortedWith(
        compareByDescending<VoiceChoice> {
            when {
                isIndonesian(it.language) && (it.name.equals("local(idd)", true) ||
                    Regex("(^|[-_])idd($|[-_])", RegexOption.IGNORE_CASE).containsMatchIn(it.name)) -> 4
                isIndonesian(it.language) -> 3
                it.language == systemLanguage -> 2
                it.language == "en" -> 1
                else -> 0
            }
        }.thenByDescending { it.quality }.thenBy { it.name }
    ).firstOrNull()

fun versionParts(value: String): List<Int>? {
    val match = Regex("^v?(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)$").matchEntire(value)
        ?: return null
    return match.groupValues.drop(1).map { it.toIntOrNull() ?: return null }
}

fun isNewer(candidate: String, current: String): Boolean {
    val new = versionParts(candidate) ?: return false
    val old = versionParts(current) ?: return false
    return new.indices.firstOrNull { new[it] != old[it] }?.let { new[it] > old[it] } ?: false
}
