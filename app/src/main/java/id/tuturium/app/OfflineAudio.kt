package id.tuturium.app

import android.content.Context
import android.os.SystemClock
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

internal data class OfflinePass(val index: Int, val speech: PreparedSpeech, val pauseAfterMs: Int = 0)

internal class SupertonicBuffer {
    private var inferenceRatio = 0.0
    private var measuredSteps = 8
    private var firstReadyAt = -1L
    private var previousSteps = 8

    fun generated(durationMs: Long, elapsedMs: Long, nowMs: Long, cacheHit: Boolean = false,
        steps: Int = 8) {
        if (firstReadyAt < 0) firstReadyAt = nowMs
        if (cacheHit) return
        val sample = elapsedMs.toDouble() / durationMs.coerceAtLeast(1L)
        val expected = estimate(steps)
        inferenceRatio = if (expected == 0.0) sample else expected * 0.70 + sample * 0.30
        measuredSteps = steps
    }

    // The duration predictor, encoder and vocoder also run at lower step counts.
    private fun estimate(steps: Int): Double =
        inferenceRatio * (0.25 + 0.75 * steps / 8.0) / (0.25 + 0.75 * measuredSteps / 8.0)

    fun inferenceSteps(playbackRate: Float, remainingMs: Long, filling: Boolean, thermal: Int): Int {
        val runway = remainingMs / playbackRate
        val sixStepDemand = estimate(6) * playbackRate
        val eightStepDemand = estimate(8) * playbackRate
        val fastPlayback = playbackRate >= 2f
        previousSteps = when {
            thermal >= android.os.PowerManager.THERMAL_STATUS_SEVERE -> 5
            fastPlayback && (filling || runway < if (previousSteps == 5) 16_000 else 8_000) -> 5
            fastPlayback && sixStepDemand >= if (previousSteps == 5) 0.86 else 1.0 -> 5
            thermal >= android.os.PowerManager.THERMAL_STATUS_MODERATE -> 6
            fastPlayback && (runway < if (previousSteps < 8) 36_000 else 20_000) -> 6
            eightStepDemand >= if (previousSteps < 8) 0.74 else 0.88 -> 6
            else -> 8
        }
        return previousSteps
    }

    fun ready(playbackRate: Float, bufferedMs: Long, count: Int, endOfInput: Boolean, nowMs: Long): Boolean {
        if (count == 0) return false
        val demand = inferenceRatio * playbackRate
        val targetSeconds = when {
            demand >= 1.12 -> 12
            demand >= 1.0 -> 7
            demand >= 0.65 || playbackRate >= 2f -> 5
            else -> 3
        }
        val minimumClips = if (playbackRate >= 2f) 4 else 2
        return endOfInput || bufferedMs >= playbackRate * targetSeconds * 1_000 ||
            (count >= minimumClips && demand < 0.45) ||
            count >= 12 || (playbackRate < 2f && nowMs - firstReadyAt >= 8_000)
    }

    fun atCapacity(playbackRate: Float, remainingMs: Long, count: Int, thermal: Int = 0): Boolean {
        val demand = inferenceRatio * playbackRate
        val target = when {
            thermal >= android.os.PowerManager.THERMAL_STATUS_SEVERE -> 45_000L
            thermal >= android.os.PowerManager.THERMAL_STATUS_MODERATE -> 90_000L
            demand >= 0.85 -> 300_000L
            demand >= 0.55 -> 180_000L
            else -> 90_000L
        }
        // The current model outputs 44.1 kHz PCM. Bound disk lookahead to about 63 MiB plus one clip.
        val limit = minOf((playbackRate * target).toLong(), 750_000L)
        return count >= 384 || (count > 1 && remainingMs >= limit)
    }

    fun compressionFits(playbackRate: Float, remainingMs: Long, thermal: Int): Boolean =
        thermal < android.os.PowerManager.THERMAL_STATUS_MODERATE &&
            remainingMs >= playbackRate * 18_000 && inferenceRatio * playbackRate < 0.8

    fun recoveryReady(playbackRate: Float, bufferedMs: Long, count: Int, finished: Boolean): Boolean {
        val reserve = if (inferenceRatio * playbackRate >= 0.9) 12_000 else 6_000
        return count > 0 && (finished || bufferedMs >= playbackRate * reserve || count >= 10)
    }
}

/** Maintains one Media3 playlist for narration, with bounded PCM lookahead. */
internal class OfflineAudio(
    private val context: Context,
    private val synthesize: suspend (Context, OfflineVoice, String, File, Int, Boolean, Int) -> SynthesizedAudio = OfflineSynth::write
) {
    private var player: ExoPlayer? = null
    private var enhancer: android.media.audiofx.LoudnessEnhancer? = null
    private var effectSession = 0
    private var narrationVolume = 1f
    private var producer: Job? = null
    private var progress: Job? = null
    private var rebuffering: Job? = null
    private var bufferChanged: (() -> Unit)? = null
    private val clips = linkedMapOf<String, Clip>()
    private var active: Clip? = null

    private data class Clip(val id: String, val pass: OfflinePass, val file: File, val audio: SynthesizedAudio)

    fun setRate(speed: Float, pitch: Float) {
        val player = player ?: return
        val parameters = PlaybackParameters(speed.coerceIn(0.5f, 4f), pitch.coerceIn(0.5f, 2f))
        if (player.playbackParameters != parameters) {
            player.playbackParameters = parameters
            bufferChanged?.invoke()
        }
    }

    fun setVolume(volume: Float) {
        narrationVolume = volume.coerceIn(0f, 1f)
        player?.volume = narrationVolume
        runCatching { enhancer?.setTargetGain(narrationBoostMb(narrationVolume)) }
    }

    private fun attachEnhancer(id: Int) {
        if (id == effectSession) return
        enhancer?.release()
        enhancer = null
        effectSession = id
        if (id <= 0) return
        runCatching {
            val effect = android.media.audiofx.LoudnessEnhancer(id)
            try {
                effect.setTargetGain(narrationBoostMb(narrationVolume))
                check(effect.setEnabled(true) == android.media.audiofx.AudioEffect.SUCCESS)
                enhancer = effect
            } catch (error: Exception) {
                effect.release()
                throw error
            }
        }
    }

    fun stop() {
        val old = player
        player = null
        producer?.cancel()
        progress?.cancel()
        rebuffering?.cancel()
        rebuffering = null
        bufferChanged = null
        producer = null
        progress = null
        active = null
        enhancer?.release()
        enhancer = null
        effectSession = 0
        old?.release()
        clips.values.forEach { it.file.delete() }
        clips.clear()
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun play(
        scope: CoroutineScope, voice: OfflineVoice, passages: Sequence<OfflinePass>, speed: Float, pitch: Float,
        volume: Float = 1f, onPass: (OfflinePass) -> Unit, onSpoken: (OfflinePass) -> Unit,
        onRange: (IntRange) -> Unit, onRendering: (Boolean) -> Unit,
        onDone: () -> Unit, onError: (Throwable) -> Unit
    ) {
        stop()
        val engine = ExoPlayer.Builder(context).build()
        player = engine
        // Hold CPU while Media3 renders or buffers, including when the display sleeps.
        engine.setWakeMode(C.WAKE_MODE_LOCAL)
        val room = Channel<Unit>(Channel.CONFLATED)
        val buffer = SupertonicBuffer()
        bufferChanged = { room.trySend(Unit) }
        var sourceDone = false
        var started = false
        var queuedMs = 0L
        var recoveryStart: String? = null
        var recovering = false
        var lastThermalSample = -2_000L
        var thermal = 0
        val power = context.getSystemService(android.os.PowerManager::class.java)
        var rendering = false
        engine.setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH).build(), false)
        setRate(speed, pitch)
        setVolume(volume)
        attachEnhancer(engine.audioSessionId)

        fun finishPass() {
            val previous = active ?: return
            active = null
            onSpoken(previous.pass)
        }

        fun fail(error: Throwable) {
            if (player !== engine) return
            stop()
            onError(error)
        }

        fun done() {
            if (player !== engine) return
            stop()
            onDone()
        }

        fun startPass(id: String?) {
            val next = clips[id] ?: return
            if (next === active) return
            finishPass()
            active = next
            onPass(next.pass)
            next.audio.cues.firstOrNull()?.range?.let {
                onRange(next.pass.speech.offsets[it.first]..next.pass.speech.offsets[it.last])
            }
        }

        engine.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) { attachEnhancer(audioSessionId) }
            override fun onPlayerError(error: PlaybackException) = fail(error)

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (player === engine && active != null && reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    startPass(mediaItem?.mediaId)
                }
            }

            override fun onEvents(player: Player, events: Player.Events) {
                if (this@OfflineAudio.player !== engine) return
                if (engine.playbackState == Player.STATE_ENDED) {
                    finishPass()
                    if (sourceDone && recoveryStart == null) {
                        done()
                        return
                    }
                    if (!recovering) {
                        recovering = true
                        engine.pause()
                    }
                } else if (engine.isPlaying) {
                    startPass(engine.currentMediaItem?.mediaId)
                }
                if (engine.isPlaying) {
                    rebuffering?.cancel()
                    rebuffering = null
                    if (!rendering) {
                        rendering = true
                        onRendering(true)
                    }
                } else if (rendering && rebuffering == null) {
                    rebuffering = scope.launch {
                        delay(200)
                        rebuffering = null
                        if (this@OfflineAudio.player === engine && !engine.isPlaying) {
                            rendering = false
                            onRendering(false)
                        }
                    }
                }
                val consumed = engine.currentMediaItemIndex
                if (consumed > 0) {
                    // The decoder has moved ahead. Remove the consumed prefix and wake the producer.
                    val ids = List(consumed) { engine.getMediaItemAt(it).mediaId }
                    engine.removeMediaItems(0, consumed)
                    ids.forEach { id ->
                        clips.remove(id)?.let {
                            queuedMs -= it.audio.durationMs
                            it.file.delete()
                        }
                    }
                    room.trySend(Unit)
                }
            }
        })
        fun prepareWhenBuffered() {
            val pending = recoveryStart
            if (recovering && pending != null) {
                val position = (0 until engine.mediaItemCount).firstOrNull { engine.getMediaItemAt(it).mediaId == pending }
                    ?: return
                val count = engine.mediaItemCount - position
                val remaining = clips.values.drop(position).sumOf { it.audio.durationMs }
                if (buffer.recoveryReady(engine.playbackParameters.speed, remaining, count, sourceDone)) {
                    recoveryStart = null
                    recovering = false
                    engine.seekToDefaultPosition(position)
                    engine.play()
                }
                return
            }
            if (!started && buffer.ready(engine.playbackParameters.speed,
                    queuedMs, clips.size, sourceDone,
                    SystemClock.elapsedRealtime())
            ) {
                started = true
                engine.prepare()
            }
        }

        engine.playWhenReady = true
        progress = scope.launch {
            var lastClip: Clip? = null
            var lastRange: IntRange? = null
            while (player === engine) {
                if (!started || recovering) prepareWhenBuffered()
                val clip = active
                if (clip != lastClip) {
                    lastClip = clip
                    lastRange = null
                }
                if (clip != null && engine.isPlaying && engine.currentMediaItem?.mediaId == clip.id) {
                    val cue = clip.audio.cues.lastOrNull { it.startMs <= engine.currentPosition }
                    val range = cue?.range?.let {
                        clip.pass.speech.offsets[it.first]..clip.pass.speech.offsets[it.last]
                    }
                    if (range != null && range != lastRange) {
                        lastRange = range
                        onRange(range)
                    }
                }
                delay(60)
            }
        }
        fun bufferAtCapacity(): Boolean {
            val now = SystemClock.elapsedRealtime()
            if (android.os.Build.VERSION.SDK_INT >= 29 && now - lastThermalSample >= 2_000L) {
                thermal = power.currentThermalStatus
                lastThermalSample = now
            }
            return buffer.atCapacity(engine.playbackParameters.speed, queuedMs - engine.currentPosition,
                clips.size, thermal)
        }

        producer = scope.launch {
            try {
                OfflineSynth.withWarmModel {
                    for (pass in passages) {
                        while (bufferAtCapacity()) {
                            withTimeoutOrNull(2_000L) { room.receive() }
                        }
                        val id = pass.index.toString()
                        val file = File.createTempFile("offline-voice-", ".wav", context.cacheDir)
                        try {
                            val startMs = SystemClock.elapsedRealtime()
                            val rate = engine.playbackParameters.speed
                            val remaining = (queuedMs - engine.currentPosition).coerceAtLeast(0)
                            val steps = buffer.inferenceSteps(rate, remaining, !started || recovering, thermal)
                            val compress = started && !recovering && buffer.compressionFits(rate, remaining, thermal)
                            val audio = synthesize(context, voice, pass.speech.text, file, steps, compress, pass.pauseAfterMs)
                            ensureActive()
                            val finishedMs = SystemClock.elapsedRealtime()
                            buffer.generated(audio.durationMs, finishedMs - startMs, finishedMs, audio.cacheHit, steps)
                            clips[id] = Clip(id, pass, file, audio)
                            queuedMs += audio.durationMs
                            if (engine.playbackState == Player.STATE_ENDED && !recovering) {
                                finishPass()
                                recovering = true
                                engine.pause()
                            }
                            if (recovering && recoveryStart == null) recoveryStart = id
                            val item = MediaItem.Builder().setMediaId(id).setUri(android.net.Uri.fromFile(file))
                            if (audio.opus) item.setMimeType("audio/ogg")
                            engine.addMediaItem(item.build())
                            prepareWhenBuffered()
                        } finally {
                            if (clips[id]?.file != file) file.delete()
                        }
                    }
                    sourceDone = true
                    prepareWhenBuffered()
                    if (clips.isEmpty() || (recovering && recoveryStart == null)) {
                        finishPass()
                        done()
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                fail(error)
            }
        }
    }
}
