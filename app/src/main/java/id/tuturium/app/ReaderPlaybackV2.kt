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
import java.util.Locale

@Composable
fun ReaderPlaybackBar(model: TuturiumModel, book: Book, document: ReadingDocument,
    playback: Playback, current: Int, playing: Boolean, loading: Boolean,
    speedPanel: () -> Unit, voicePanel: () -> Unit, seek: (Int) -> Unit) {
    val progress = (current + 1f) / document.passes.size.coerceAtLeast(1)
    val actual = if (playing) playback.effectiveSpeed else model.settings.speed
    val wholeSeconds = (book.words * 60f / (150f * actual.coerceAtLeast(0.5f))).toInt().coerceAtLeast(1)
    val elapsedSeconds = (wholeSeconds * progress).toInt()
    Surface(color = Color.Black,
        shape = RoundedCornerShape(topStart = 23.dp, topEnd = 23.dp)) {
        Column(Modifier.fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = SpeechifyAccent, trackColor = Color(0xFF383838))
            Spacer(Modifier.height(9.dp))
            Text("≈ ${formatPlaybackTime(elapsedSeconds)} / ${formatPlaybackTime(wholeSeconds)}",
                color = SpeechifyMuted, style = MaterialTheme.typography.labelMedium)
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
                Surface(onClick = speedPanel, color = Color(0xFF272727),
                    modifier = Modifier.size(56.dp), shape = CircleShape) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(String.format(Locale.forLanguageTag("id-ID"), "%.1f×", actual),
                            color = Color.White, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            if (playback.timerUntil > SystemClock.elapsedRealtime()) {
                Text("Timer tidur aktif", color = SpeechifyMuted,
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun formatPlaybackTime(seconds: Int): String =
    "%02d:%02d".format(Locale.US, seconds / 60, seconds % 60)
