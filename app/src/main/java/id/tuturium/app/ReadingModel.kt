package id.tuturium.app

import java.text.BreakIterator
import java.util.Locale

data class Section(val title: String, val text: String)
data class Pass(val text: String, val section: Int, val paragraph: Int, val offset: Int)

data class ReadingDocument(val sections: List<Section>) {
    internal val paragraphs = sections.map { it.text.split(Regex("\\n+")).filter(String::isNotBlank) }
    val passes: List<Pass> = paragraphs.flatMapIndexed { section, paragraphs ->
        paragraphs.flatMapIndexed { paragraph, text -> splitSpeech(text, section, paragraph) }
    }
}

/** PDF section labels preserve page numbers even in old imports that omitted empty pages. */
internal fun ReadingDocument.pdfPageForPass(index: Int): Int {
    val section = passes.getOrNull(index)?.section ?: return 0
    return pdfPageForSection(section)
}

internal fun ReadingDocument.firstPassByPdfPage(): Map<Int, Int> =
    buildMap {
        for ((index, pass) in passes.withIndex()) {
            val page = pdfPageForSection(pass.section)
            if (!containsKey(page)) put(page, index)
        }
    }

private fun ReadingDocument.pdfPageForSection(index: Int): Int =
    sections[index].title.removePrefix("Halaman ").toIntOrNull()
        ?.takeIf { it > 0 }?.minus(1) ?: index

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


/** A reader tap addresses a word within an existing TTS sentence/pass. */
data class WordSelection(val passIndex: Int, val offset: Int)

/** Maps a visual character tap to the start of the nearest readable word, not a paragraph. */
fun selectSpokenWord(rowText: String, passes: List<IndexedValue<Pass>>,
    tapOffset: Int): WordSelection? {
    if (rowText.isEmpty() || passes.isEmpty()) return null
    val location = tapOffset.coerceIn(0, rowText.lastIndex)
    fun isWord(index: Int) = rowText[index].isLetterOrDigit()
    val anchor = if (isWord(location)) location else {
        val before = (location downTo 0).firstOrNull(::isWord)
        val after = (location + 1..rowText.lastIndex).firstOrNull(::isWord)
        when {
            before == null -> after
            after == null -> before
            location - before <= after - location -> before
            else -> after
        } ?: return null
    }
    var start = anchor
    while (start > 0 && isWord(start - 1)) start--
    val pass = passes.firstOrNull {
        start >= it.value.offset && start < it.value.offset + it.value.text.length
    } ?: return null
    return WordSelection(pass.index, start - pass.value.offset)
}

data class VoiceChoice(val name: String, val language: String, val label: String, val quality: Int, val network: Boolean = false)

internal fun isIndonesian(language: String) = language == "id" || language == "in" || language == "ind"

/** Expose only actual Indonesian voices returned by Google Speech Services. */
internal const val GOOGLE_TTS_ENGINE = "com.google.android.tts"
internal const val GOOGLE_ID_AUTO = "id-ID-google-automatic"
private val preferredGoogleVoiceIds = listOf(
    "id-id-x-dfz-local", "id-id-x-idc-local", "id-id-x-idd-local", "id-id-x-ide-local"
)

fun googleIndonesianVoices(voices: List<VoiceChoice>): List<VoiceChoice> =
    voices.filter { isIndonesian(it.language) }
        .distinctBy { it.name }
        .sortedWith(compareBy<VoiceChoice> {
            val index = preferredGoogleVoiceIds.indexOf(it.name.lowercase(Locale.ROOT))
            if (index < 0) 100 else index
        }.thenBy { it.network }.thenBy { it.name })

fun indonesianVoiceLabel(voice: VoiceChoice, all: List<VoiceChoice>): String {
    val index = googleIndonesianVoices(all).indexOfFirst { it.name == voice.name }
    return if (voice.name == GOOGLE_ID_AUTO) "Google Indonesia otomatis"
        else if (index >= 0) "Suara Indonesia ${index + 1}" else "Suara Indonesia"
}

/** Old Android TTS engines can change voice IDs after updates. Never fall back to English. */
fun resolveIndonesianVoice(voices: List<VoiceChoice>, selected: String?,
    allowNetwork: Boolean = true): VoiceChoice? {
    val eligible = googleIndonesianVoices(voices).filter { allowNetwork || !it.network }
    return eligible.firstOrNull { it.name == selected } ?:
        eligible.firstOrNull { it.name.lowercase(Locale.ROOT) == "id-id-x-idd-local" } ?:
        eligible.firstOrNull()
}


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
        }.thenBy { it.network }.thenByDescending { it.quality }.thenBy { it.name }
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
