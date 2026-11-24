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

internal data class OfflinePass(val index: Int, val speech: PreparedSpeech)

internal class SupertonicBuffer {
    private var inferenceRatio = 0.0
    private var firstReadyAt = -1L

    fun generated(durationMs: Long, elapsedMs: Long, nowMs: Long) {
        val sample = elapsedMs.toDouble() / durationMs.coerceAtLeast(1L)
        inferenceRatio = if (firstReadyAt < 0) sample else inferenceRatio * 0.75 + sample * 0.25
        if (firstReadyAt < 0) firstReadyAt = nowMs
    }

    fun ready(playbackRate: Float, bufferedMs: Long, count: Int, endOfInput: Boolean, nowMs: Long): Boolean {
        if (count == 0) return false
        val targetMs = if (playbackRate >= 2f || inferenceRatio * playbackRate > 0.75)
            (playbackRate * 12_000).toLong() else (playbackRate * 6_000).toLong()
        val minimumClips = if (playbackRate >= 2f) 4 else 2
        return endOfInput || bufferedMs >= targetMs ||
            (count >= minimumClips && inferenceRatio * playbackRate < 0.45) ||
            count >= 12 || nowMs - firstReadyAt >= 8_000
    }

    fun atCapacity(playbackRate: Float, remainingMs: Long, count: Int): Boolean =
        count >= 56 || (count > 1 && remainingMs >= maxOf(90_000L, (playbackRate * 48_000).toLong()))
}

/** Maintains one Media3 playlist for narration, with bounded PCM lookahead. */
internal class OfflineAudio(private val context: Context) {
    private var player: ExoPlayer? = null
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
        old?.release()
        clips.values.forEach { it.file.delete() }
        clips.clear()
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun play(
        scope: CoroutineScope, voice: OfflineVoice, passages: Sequence<OfflinePass>, speed: Float, pitch: Float,
        onPass: (OfflinePass) -> Unit, onSpoken: (OfflinePass) -> Unit,
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
        var rendering = false
        engine.setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH).build(), false)
        setRate(speed, pitch)

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
            override fun onPlayerError(error: PlaybackException) = fail(error)

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (player === engine && active != null && reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO)
                    startPass(mediaItem?.mediaId)
            }

            override fun onEvents(player: Player, events: Player.Events) {
                if (this@OfflineAudio.player !== engine) return
                if (engine.playbackState == Player.STATE_ENDED) {
                    finishPass()
                    if (sourceDone) { done(); return }
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
                    ids.forEach { clips.remove(it)?.file?.delete() }
                    room.trySend(Unit)
                }
            }
        })
        fun prepareWhenBuffered() {
            if (!started && buffer.ready(engine.playbackParameters.speed,
                    clips.values.sumOf { it.audio.durationMs }, clips.size, sourceDone,
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
                if (!started) prepareWhenBuffered()
                val clip = active
                if (clip != lastClip) { lastClip = clip; lastRange = null }
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
            val remainingMs = clips.values.sumOf { it.audio.durationMs } - engine.currentPosition
            return buffer.atCapacity(engine.playbackParameters.speed, remainingMs, clips.size)
        }

        producer = scope.launch {
            try {
                OfflineSynth.withWarmModel {
                    for (pass in passages) {
                        while (bufferAtCapacity()) {
                            room.receive()
                        }
                        val id = pass.index.toString()
                        val file = File.createTempFile("offline-voice-", ".wav", context.cacheDir)
                        try {
                            val startMs = SystemClock.elapsedRealtime()
                            val audio = OfflineSynth.write(context, voice, pass.speech.text, file)
                            ensureActive()
                            val finishedMs = SystemClock.elapsedRealtime()
                            buffer.generated(audio.durationMs, finishedMs - startMs, finishedMs)
                            clips[id] = Clip(id, pass, file, audio)
                            val restart = engine.playbackState == Player.STATE_ENDED
                            val item = MediaItem.Builder().setMediaId(id).setUri(android.net.Uri.fromFile(file)).build()
                            engine.addMediaItem(item)
                            if (restart) engine.seekToDefaultPosition(engine.mediaItemCount - 1)
                            prepareWhenBuffered()
                        } finally {
                            if (clips[id]?.file != file) file.delete()
                        }
                    }
                    sourceDone = true
                    prepareWhenBuffered()
                    if (clips.isEmpty() || engine.playbackState == Player.STATE_ENDED) {
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
