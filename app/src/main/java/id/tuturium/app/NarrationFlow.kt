package id.tuturium.app

private val abbreviations = Regex("""(?<![\p{L}\p{N}.])(?:s\.d\.|dr\.|prof\.|hlm\.|dll\.|dsb\.|dst\.)(?![\p{L}\p{N}])""",
    RegexOption.IGNORE_CASE)

private fun expandedAbbreviation(value: String): String = when (value.lowercase(java.util.Locale.ROOT)) {
    "dr." -> if (value.startsWith("Dr")) "Doktor" else "dokter"
    "prof." -> "profesor"
    "hlm." -> "halaman"
    "dll." -> "dan lain-lain"
    "dsb." -> "dan sebagainya"
    "dst." -> "dan seterusnya"
    else -> "sampai dengan"
}

private fun normalizedCharacter(char: Char): String = when (char) {
    '\u00ad', '\u200b' -> ""
    '\u00a0', '\u202f', '\r', '\n', '\t' -> " "
    '\ufb01' -> "fi"
    '\ufb02' -> "fl"
    else -> char.toString()
}

internal fun PreparedSpeech.normalizedForNarration(): PreparedSpeech {
    val matches = abbreviations.findAll(text).iterator()
    var match = if (matches.hasNext()) matches.next() else null
    val normalized = StringBuilder(text.length)
    val sourceOffsets = ArrayList<Int>(text.length)
    var index = 0
    while (index < text.length) {
        val replacement = match?.takeIf { it.range.first == index }
        val value = if (replacement == null) normalizedCharacter(text[index])
            else expandedAbbreviation(replacement.value)
        for (character in value) {
            normalized.append(character)
            sourceOffsets += offsets[index]
        }
        index = if (replacement == null) index + 1 else replacement.range.last + 1
        if (replacement != null) match = if (matches.hasNext()) matches.next() else null
    }
    return PreparedSpeech(normalized.toString(), sourceOffsets.toIntArray())
}

private fun String.withoutClosingPunctuation() = trimEnd().trimEnd('"', '\'', '”', '’', '»', ')', ']', '}')

private val pageLabels = Regex("^Halaman ([1-9][0-9]*)$")

internal fun ReadingDocument.continuesAcrossPdfPage(index: Int): Boolean {
    val previous = passes.getOrNull(index) ?: return false
    val next = passes.getOrNull(index + 1) ?: return false
    if (previous.section + 1 != next.section || next.paragraph != 0) return false
    val from = pageLabels.matchEntire(sections[previous.section].title)?.groupValues?.get(1)?.toIntOrNull() ?: return false
    val to = pageLabels.matchEntire(sections[next.section].title)?.groupValues?.get(1)?.toIntOrNull()
    val tail = previous.text.withoutClosingPunctuation().lastOrNull()
    return to == from + 1 && tail != null && (tail.isLetter() || tail in ",;:") &&
        next.text.firstOrNull()?.isLowerCase() == true
}

internal data class NarrationPause(val naturalMs: Int, val extraMs: Int = 0) {
    val totalMs get() = naturalMs + extraMs
}

internal fun ReadingDocument.pauseAfter(index: Int, options: ListeningOptions): NarrationPause {
    val pass = passes.getOrNull(index) ?: return NarrationPause(0)
    val next = passes.getOrNull(index + 1) ?: return NarrationPause(0)
    if (continuesAcrossPdfPage(index)) return NarrationPause(12)
    val text = pass.text.withoutClosingPunctuation()
    val abbreviation = abbreviations.findAll(text).lastOrNull()?.range?.last == text.lastIndex
    if (abbreviation && next.section == pass.section && next.paragraph == pass.paragraph) return NarrationPause(18)
    val paragraph = pass.section != next.section || pass.paragraph != next.paragraph
    val natural = when {
        paragraph -> 180
        text.endsWith("…") || text.endsWith("...") -> 165
        text.lastOrNull() in listOf('.', '!', '?') -> 112
        text.lastOrNull() in listOf(',', ';', ':') -> 58
        else -> 22
    }
    return NarrationPause(natural, if (paragraph) options.paragraphPauseMs else options.sentencePauseMs)
}

internal fun clausePause(text: String): Int = when (text.withoutClosingPunctuation().lastOrNull()) {
    '.', '!', '?', '…' -> 95
    ',', ';', ':', '—' -> 45
    else -> 15
}

/** Ignore silence only at the two edges. A margin protects unvoiced consonants and model fades. */
internal fun spokenPcmRange(samples: FloatArray, rate: Int): IntRange {
    val frameSize = (rate / 100).coerceAtLeast(1)
    val edgeLimit = minOf(rate * 6 / 10, samples.size / 3)
    if (edgeLimit < frameSize * 2) return samples.indices
    fun hasVoice(start: Int, end: Int): Boolean {
        var energy = 0f
        for (index in start until end) {
            val x = samples[index]
            energy += x * x
        }
        return energy > (end - start) * 0.000025f
    }
    var left = 0
    while (left + frameSize <= edgeLimit && !hasVoice(left, left + frameSize)) left += frameSize
    var right = samples.size
    while (right - frameSize >= samples.size - edgeLimit && !hasVoice(right - frameSize, right))
        right -= frameSize
    return (left - frameSize * 2).coerceAtLeast(0) until (right + frameSize * 2).coerceAtMost(samples.size)
}
