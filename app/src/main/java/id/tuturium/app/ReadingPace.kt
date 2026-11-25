package id.tuturium.app

import android.content.Context
import android.media.AudioFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

internal data class PaceProfile(val key: String) {
    companion object {
        fun forPlayback(settings: ReadingSettings, options: ListeningOptions, speed: Float): PaceProfile {
            val offlineVoice = OfflineCatalog.voice(settings.voice)
            // Supertonic generates at 1×. The Google engine controls its own rate.
            return if (offlineVoice != null) PaceProfile("text-v2:supertonic:${offlineVoice.id}")
            else PaceProfile("text-v2:google:${settings.voice.orEmpty()}:${options.matchLanguage}:" +
                "${(settings.pitch * 20).roundToInt()}:${(speed * 4).roundToInt()}")
        }
    }
}

/** Preserve the estimate for each voice/rate profile until the reader is reopened. */
internal class NarrationPaceSnapshot(private val calibratedPace: (PaceProfile) -> Double) {
    private val estimates = mutableMapOf<PaceProfile, Double>()

    fun millisPerWord(profile: PaceProfile): Double =
        estimates.getOrPut(profile) { calibratedPace(profile) }
}

internal data class PaceSample(val words: Int, val millisAtNormalSpeed: Double) {
    val millisPerWord: Double get() = millisAtNormalSpeed / words

    fun combinedWith(next: PaceSample): PaceSample {
        val retained = words.coerceAtMost(600)
        return PaceSample(retained + next.words, millisPerWord * retained + next.millisAtNormalSpeed)
    }
}

/** On-device pace calibration. Only aggregate counts and timing are persisted. */
internal object ReadingPace {
    private val latest = mutableMapOf<PaceProfile, PaceSample>()
    private val pendingWords = mutableMapOf<PaceProfile, Int>()
    private val unchanged = MutableStateFlow(0)
    val revision = unchanged.asStateFlow()

    private fun storage(context: Context) = context.getSharedPreferences("reading_pace", Context.MODE_PRIVATE)

    fun millisPerWord(context: Context, profile: PaceProfile): Double =
        sample(context, profile)?.takeIf { it.words >= 20 }?.millisPerWord ?: INITIAL_MILLIS_PER_WORD

    // Both the UI and ReadingService access this on the main thread.
    fun record(context: Context, profile: PaceProfile, words: Int, millisAtNormalSpeed: Double) {
        if (words <= 0) return
        val next = PaceSample(words, millisAtNormalSpeed)
        if (next.millisPerWord !in 75.0..2_500.0) return
        val previous = sample(context, profile)
        val updated = previous?.combinedWith(next) ?: next
        latest[profile] = updated
        val unflushed = (pendingWords[profile] ?: 0) + words
        val learned = updated.words >= 20
        val firstLearned = learned && (previous?.words ?: 0) < 20
        if (firstLearned || unflushed >= 100) {
            storage(context).edit().putInt("${profile.key}:words", updated.words)
                .putFloat("${profile.key}:ms", updated.millisAtNormalSpeed.toFloat()).apply()
            pendingWords.remove(profile)
        } else pendingWords[profile] = unflushed
        if (learned) unchanged.value++
    }

    private fun sample(context: Context, profile: PaceProfile): PaceSample? =
        latest[profile] ?: storage(context).let { prefs ->
            val words = prefs.getInt("${profile.key}:words", 0)
            val millis = prefs.getFloat("${profile.key}:ms", 0f).toDouble()
            if (words > 0 && millis > 0) PaceSample(words, millis).also { latest[profile] = it }
            else null
        }
}

/** Android reports PCM on the TTS callback thread. Count bytes without retaining document audio. */
internal class UtteranceAudioLength {
    private var utterance: String? = null
    private var bytesPerSecond = 0L
    private var bytes = 0L

    @Synchronized
    fun reset(id: String?) {
        utterance = id
        bytesPerSecond = 0
        bytes = 0
    }

    @Synchronized
    fun begin(id: String?, rate: Int, format: Int, channels: Int) {
        if (id == null || id != utterance) return
        val bytesPerSample = when (format) {
            AudioFormat.ENCODING_PCM_8BIT -> 1
            AudioFormat.ENCODING_PCM_16BIT -> 2
            AudioFormat.ENCODING_PCM_FLOAT -> 4
            else -> 0
        }
        bytesPerSecond = rate.toLong() * channels * bytesPerSample
    }

    @Synchronized
    fun add(id: String?, size: Int) {
        if (id != null && id == utterance && bytesPerSecond > 0) bytes += size
    }

    @Synchronized
    fun finish(id: String?): Double? {
        if (id == null || id != utterance || bytesPerSecond <= 0 || bytes <= 0) return null
        val durationMs = 1_000.0 * bytes / bytesPerSecond
        reset(null)
        return durationMs
    }
}
