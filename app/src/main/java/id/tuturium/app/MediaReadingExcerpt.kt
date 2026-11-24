package id.tuturium.app

/** Compact, stable phrase windows for system media controls, using original text offsets. */
internal fun mediaReadingExcerpt(text: String, offset: Int, maxChars: Int = 64): String {
    if (text.isBlank()) return ""
    val words = Regex("\\S+").findAll(text).toList()
    if (words.isEmpty()) return ""
    val anchor = words.indexOfLast { it.range.first <= offset.coerceIn(0, text.lastIndex) }.coerceAtLeast(0)
    var start = 0
    while (start < words.size) {
        var end = start + 1
        var length = words[start].value.length
        while (end < words.size && words[end - 1].value.last() !in ".!?" &&
            length + 1 + words[end].value.length <= maxChars.coerceAtLeast(8)) {
            length += 1 + words[end].value.length
            end++
        }
        if (anchor < end) {
            val phrase = words.subList(start, end).joinToString(" ") { it.value }
            return if (phrase.length <= maxChars.coerceAtLeast(8)) phrase
                else phrase.take(maxChars.coerceAtLeast(8) - 1).trimEnd() + "…"
        }
        start = end
    }
    return words.last().value
}
