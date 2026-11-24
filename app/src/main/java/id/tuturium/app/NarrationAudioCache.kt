package id.tuturium.app

import java.io.File
import java.security.MessageDigest
import kotlin.math.abs
import kotlin.math.sqrt

internal fun narrationGain(samples: FloatArray): Float {
    var sum = 0.0
    var count = 0
    for (sample in samples) {
        if (!sample.isFinite() || abs(sample) < 0.015f) continue
        sum += sample.toDouble() * sample
        count++
    }
    if (count == 0) return 1f
    return (0.32 / sqrt(sum / count)).toFloat().coerceIn(1f, 8f)
}

internal fun amplifyNarration(sample: Float, gain: Float): Float {
    if (!sample.isFinite()) return 0f
    val boosted = (sample * gain).coerceIn(-4f, 4f)
    val excess = abs(boosted) - 0.65f
    if (excess <= 0f) return boosted
    return Math.copySign(0.65f + 0.31f * excess / (0.31f + excess), boosted)
}

/** Regenerable app-private cache. A complete entry is published with a single rename. */
internal class NarrationAudioCache(private val directory: File, private val budget: Long = 96L * 1024 * 1024) {
    private val maxAge = 7L * 24 * 60 * 60 * 1000

    private fun entry(text: String, configuration: String): File {
        val key = MessageDigest.getInstance("SHA-256")
        key.update(configuration.toByteArray())
        key.update(0)
        val hex = key.digest(text.toByteArray()).joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }
        return File(directory, "$hex.pcmcache")
    }

    fun restore(text: String, configuration: String, target: File): SynthesizedAudio? {
        val entry = entry(text, configuration)
        if (!entry.isFile || System.currentTimeMillis() - entry.lastModified() > maxAge) return null
        return runCatching {
            java.io.DataInputStream(entry.inputStream().buffered()).use { input ->
                val duration = input.readLong()
                val count = input.readInt()
                check(duration > 0 && count in 1..text.length)
                val cues = List(count) {
                    val time = input.readLong()
                    val start = input.readInt()
                    val end = input.readInt()
                    check(time in 0 until duration && start in text.indices && end in start until text.length)
                    AudioCue(time, start..end)
                }
                val size = input.readLong()
                check(size > 44 && size == entry.length() - (20L + 16L * count))
                target.outputStream().buffered().use { output -> check(input.copyTo(output) == size) }
                entry.setLastModified(System.currentTimeMillis())
                SynthesizedAudio(cues, duration, cacheHit = true)
            }
        }.getOrElse {
            entry.delete()
            null
        }
    }

    fun save(text: String, configuration: String, source: File, audio: SynthesizedAudio) {
        val entry = entry(text, configuration)
        val size = source.length() + 20L + 16L * audio.cues.size
        if (size > budget || (!directory.isDirectory && !directory.mkdirs())) return
        runCatching {
            val now = System.currentTimeMillis()
            val entries = directory.listFiles { file -> file != entry }.orEmpty()
                .map { it to it.lastModified() }.sortedBy { (_, modified) -> modified }
            var total = entries.sumOf { (file, _) -> file.length() }
            for ((old, modified) in entries) {
                if (total + size <= budget && now - modified <= maxAge) break
                val length = old.length()
                if (old.delete()) total -= length
            }
            if (total + size > budget) return
            val staging = File.createTempFile("pending-", ".part", directory)
            try {
                java.io.DataOutputStream(staging.outputStream().buffered()).use { output ->
                    output.writeLong(audio.durationMs)
                    output.writeInt(audio.cues.size)
                    for (cue in audio.cues) {
                        output.writeLong(cue.startMs)
                        output.writeInt(cue.range.first)
                        output.writeInt(cue.range.last)
                    }
                    output.writeLong(source.length())
                    source.inputStream().buffered().use { it.copyTo(output) }
                }
                staging.renameTo(entry)
            } finally {
                staging.delete()
            }
        }
    }
}
