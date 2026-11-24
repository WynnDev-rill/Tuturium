package id.tuturium.app

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.os.SystemClock
import androidx.annotation.RequiresApi
import java.io.File
import java.io.DataInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Compresses a generated mono PCM16 WAV for the replay cache. Live playback keeps its WAV. */
@RequiresApi(Build.VERSION_CODES.Q)
internal fun encodeNarrationOpus(source: File, output: File, sampleRate: Int, checkActive: () -> Unit) {
    val opusSampleRate = 48_000
    val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_OPUS, opusSampleRate, 1).apply {
        setInteger(MediaFormat.KEY_BIT_RATE, 32_000)
    }
    val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_OPUS)
    try {
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec.start()
        val muxer = MediaMuxer(output.path, MediaMuxer.OutputFormat.MUXER_OUTPUT_OGG)
        try {
            val info = MediaCodec.BufferInfo()
            var track = -1
            var ended = false
            var inputFrames = 0L
            val deadline = SystemClock.elapsedRealtime() + 2_000L
            DataInputStream(source.inputStream().buffered()).use { wav ->
                wav.readFully(ByteArray(44))
                val pcm = NarrationResampler(wav, (source.length() - 44) / 2, sampleRate, opusSampleRate)
                while (true) {
                    checkActive()
                    check(SystemClock.elapsedRealtime() <= deadline) { "Batas waktu encoder Opus" }
                    if (!ended) {
                        val slot = codec.dequeueInputBuffer(0)
                        if (slot >= 0) {
                            val input = checkNotNull(codec.getInputBuffer(slot))
                            input.clear()
                            input.order(ByteOrder.LITTLE_ENDIAN)
                            val frames = pcm.write(input, maxFrames = 2_048)
                            val time = inputFrames * 1_000_000 / opusSampleRate
                            ended = frames == 0
                            codec.queueInputBuffer(slot, 0, frames * 2, time,
                                if (ended) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0)
                            inputFrames += frames
                        }
                    }
                    when (val slot = codec.dequeueOutputBuffer(info, 2_000)) {
                        MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            check(track == -1)
                            track = muxer.addTrack(codec.outputFormat)
                            muxer.start()
                        }
                        MediaCodec.INFO_TRY_AGAIN_LATER -> Unit
                        else -> if (slot >= 0) {
                            if (info.size > 0 && info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG == 0) {
                                check(track >= 0)
                                muxer.writeSampleData(track, checkNotNull(codec.getOutputBuffer(slot)), info)
                            }
                            codec.releaseOutputBuffer(slot, false)
                            if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) break
                        }
                    }
                }
            }
            check(track >= 0)
            muxer.stop()
        } finally {
            muxer.release()
        }
    } finally {
        codec.release()
    }
}

/** Lightweight linear upsampling for the optional codec path, leaving the live PCM untouched. */
internal class NarrationResampler(private val pcm: DataInputStream, private var framesLeft: Long,
    private val inputRate: Int, private val outputRate: Int) {
    private var outputFramesLeft = framesLeft * outputRate / inputRate
    private var phase = 0
    private var current = takeOr(0)
    private var next = takeOr(current)

    private fun takeOr(fallback: Int): Int {
        if (framesLeft == 0L) return fallback
        framesLeft--
        return java.lang.Short.reverseBytes(pcm.readShort()).toInt()
    }

    fun write(destination: ByteBuffer, maxFrames: Int): Int {
        val count = minOf(outputFramesLeft, (destination.remaining() / 2).toLong(), maxFrames.toLong()).toInt()
        repeat(count) {
            val sample = (current * (outputRate - phase) + next * phase) / outputRate
            destination.putShort(sample.toShort())
            phase += inputRate
            while (phase >= outputRate) {
                phase -= outputRate
                current = next
                next = takeOr(current)
            }
        }
        outputFramesLeft -= count
        return count
    }
}
