package id.tuturium.app

import android.os.SystemClock
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import java.util.Locale

@Composable
internal fun ReaderPlaybackBar(model: TuturiumModel, book: Book, document: ReadingDocument,
    playback: Playback, current: Int, playing: Boolean, loading: Boolean,
    position: ReadingPosition?, speedPanel: () -> Unit, voicePanel: () -> Unit, seek: (Int) -> Unit) {
    val progress = position?.fraction ?: 0f
    val actual = playback.readingSpeed(book, model.settings.speed)
    val speed = playback.estimatedReadingSpeed(book, model.settings.speed)
    val millisPerWord = narrationMillisPerWord(model, speed, model.readerPace)
    Surface(color = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 23.dp, topEnd = 23.dp)) {
        Column(Modifier.fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = SpeechifyAccent, trackColor = MaterialTheme.colorScheme.surfaceContainerHighest)
            Spacer(Modifier.height(9.dp))
            val caption = playbackTimeCaption(position, millisPerWord, speed)
            Text(caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = voicePanel, modifier = Modifier.size(51.dp)) {
                    IndonesianVoiceAvatar(Modifier.size(48.dp))
                }
                IconButton(onClick = {
                    seek(seekBySeconds(document.passes, current, -10, actual))
                }, modifier = Modifier.size(46.dp)) {
                    Icon(Icons.Rounded.Replay10, "Mundur sekitar 10 detik", Modifier.size(34.dp))
                }
                FilledIconButton(onClick = { model.play(book, current) },
                    modifier = Modifier.size(67.dp), shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = SpeechifyAccent, contentColor = Color.White)) {
                    if (loading) CircularProgressIndicator(Modifier.size(25.dp),
                        color = Color.White, strokeWidth = 2.dp)
                    else Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        if (playing) "Jeda" else "Putar", Modifier.size(36.dp))
                }
                IconButton(onClick = {
                    seek(seekBySeconds(document.passes, current, 10, actual))
                }, modifier = Modifier.size(46.dp)) {
                    Icon(Icons.Rounded.Forward10, "Maju sekitar 10 detik", Modifier.size(34.dp))
                }
                Surface(onClick = speedPanel, color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(56.dp).semantics {
                        contentDescription = "Atur kecepatan"
                    }, shape = CircleShape) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(formatPlaybackSpeed(speed),
                            color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            if (playback.timerUntil > SystemClock.elapsedRealtime()) {
                Text("Timer tidur aktif", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

internal fun formatPlaybackSpeed(speed: Float): String =
    String.format(Locale.forLanguageTag("id-ID"), "%.2f", speed).removeSuffix("0") + "×"

internal fun formatPlaybackTime(seconds: Int): String {
    val time = seconds.coerceAtLeast(0)
    return if (time >= 3600)
        "%02d:%02d:%02d".format(Locale.US, time / 3600, time / 60 % 60, time % 60)
    else "%02d:%02d".format(Locale.US, time / 60, time % 60)
}

/** Display elapsed-equivalent and total at the selected voice's calibrated playback rate. */
internal fun playbackTimeCaption(position: ReadingPosition?, millisPerWord: Double, speed: Float): String {
    if (position == null) return "Menghitung durasi…"
    val total = position.totalSeconds(millisPerWord, speed).coerceAtLeast(0)
    val elapsed = (total - position.remainingSeconds(millisPerWord, speed)).coerceIn(0, total)
    return "≈ ${formatPlaybackTime(elapsed)} / ${formatPlaybackTime(total)}"
}
