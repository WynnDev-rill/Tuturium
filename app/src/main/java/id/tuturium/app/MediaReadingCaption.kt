package id.tuturium.app

/** Compact media subtitles. Use source offsets, not a wall-clock estimate. */
internal class MediaReadingCaption(speech: PreparedSpeech) {
    private data class Cue(val offset: Int, val label: String)
    private val cues: List<Cue> = buildList {
        val words = Regex("\\S+").findAll(speech.text).toList()
        var start = 0
        while (start < words.size) {
            var end = start + 1
            while (end < words.size && end - start < 6 &&
                words[end].range.last - words[start].range.first + 1 <= 90) end++
            val first = words[start].range.first
            val last = words[end - 1].range.last
            val offset = speech.offsets.getOrNull(first) ?: first
            val phrase = speech.text.substring(first, last + 1).replace(Regex("\\s+"), " ")
            val label = if (phrase.length <= 90) phrase else {
                val cut = if (phrase[88].isHighSurrogate()) 88 else 89
                phrase.take(cut) + "…"
            }
            add(Cue(offset, label))
            start = end
        }
    }

    fun at(sourceOffset: Int): String {
        if (cues.isEmpty()) return ""
        var low = 0
        var high = cues.size
        while (low < high) {
            val mid = (low + high) ushr 1
            if (cues[mid].offset <= sourceOffset) low = mid + 1 else high = mid
        }
        return cues[(low - 1).coerceAtLeast(0)].label
    }
}
