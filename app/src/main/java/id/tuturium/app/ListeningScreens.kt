package id.tuturium.app

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.round

internal val SpeechifyPanel = Color(0xFF202020)
internal val SpeechifyAccent = Color(0xFF4F60F7)
internal val SpeechifyMuted = Color(0xFFAAAAAA)

@Composable
private fun SwitchRow(icon: ImageVector, heading: String, detail: String? = null,
    checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = if (detail == null) 69.dp else 84.dp)
        .clickable { onChecked(!checked) }.padding(horizontal = 17.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(15.dp))
        Column(Modifier.weight(1f)) {
            Text(heading, style = MaterialTheme.typography.bodyLarge)
            if (detail != null) Text(detail, color = SpeechifyMuted,
                style = MaterialTheme.typography.bodySmall, maxLines = 2)
        }
        Spacer(Modifier.width(10.dp))
        Switch(checked, onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(checkedTrackColor = SpeechifyAccent))
    }
}

@Composable
fun ListeningSettingsV2(model: TuturiumModel, onBack: () -> Unit) {
    val p = model.listeningOptions
    LazyColumn(Modifier.fillMaxSize().background(SpeechifyPanel),
        contentPadding = PaddingValues(top = 18.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Kembali")
                }
                Text("Pengaturan Mendengarkan", style = MaterialTheme.typography.titleLarge)
            }
        }
        item { Text("Umum", modifier = Modifier.padding(start = 24.dp, top = 27.dp, bottom = 12.dp),
            style = MaterialTheme.typography.titleMedium) }
        item { SwitchRow(Icons.Rounded.PlayCircle, "Putar Audio Otomatis",
            checked = p.autoplay, onChecked = { model.changeListeningOptions(p.copy(autoplay = it)) }) }
        item { SwitchRow(Icons.Rounded.VisibilityOff, "Sembunyikan Putar Otomatis",
            checked = p.hideAutoplay, onChecked = { model.changeListeningOptions(p.copy(hideAutoplay = it)) }) }
        item { SwitchRow(Icons.Rounded.AutoAwesomeMotion, "Gulir otomatis",
            checked = p.autoScroll, onChecked = { model.changeListeningOptions(p.copy(autoScroll = it)) }) }
        item { SwitchRow(Icons.Rounded.Translate, "Cocokkan Suara dengan Bahasa",
            "Prioritaskan bahasa Indonesia saat otomatis memilih suara.",
            checked = p.matchLanguage,
            onChecked = { model.changeListeningOptions(p.copy(matchLanguage = it)) }) }
        item { SwitchRow(Icons.Rounded.MusicNote, "Padukan dengan Musik Latar",
            "Tuturium tidak mengambil audio focus; aplikasi musik lain dapat tetap berjalan.",
            checked = p.mixWithMusic,
            onChecked = { model.changeListeningOptions(p.copy(mixWithMusic = it)) }) }
        item {
            Text("Lewati Konten Secara Otomatis",
                modifier = Modifier.padding(start = 24.dp, top = 27.dp, bottom = 7.dp),
                style = MaterialTheme.typography.titleMedium)
        }
        item { SwitchRow(Icons.Rounded.Rule, "Lewati Semua",
            "Saat aktif, jenis konten di bawah akan disaring dari audio saja.",
            checked = p.skipEnabled,
            onChecked = { model.changeListeningOptions(p.copy(skipEnabled = it)) }) }
        item { HorizontalDivider(Modifier.padding(horizontal = 24.dp), color = Color(0xFF404040)) }
        item { SwitchRow(Icons.Rounded.Subtitles, "Header", "Judul bagian dan penanda bab",
            checked = p.skipHeaders, onChecked = { model.changeListeningOptions(p.copy(skipHeaders = it)) }) }
        item { SwitchRow(Icons.Rounded.VerticalAlignBottom, "Catatan kaki", "Nomor halaman dan footer",
            checked = p.skipFooters, onChecked = { model.changeListeningOptions(p.copy(skipFooters = it)) }) }
        item { SwitchRow(Icons.Rounded.Description, "Catatan kaki", "Catatan referensi bernomor",
            checked = p.skipFootnotes, onChecked = { model.changeListeningOptions(p.copy(skipFootnotes = it)) }) }
        item { HorizontalDivider(Modifier.padding(horizontal = 24.dp), color = Color(0xFF404040)) }
        item { SwitchRow(Icons.Rounded.TableChart, "Tabel", "Baris teks menyerupai tabel",
            checked = p.skipTables, onChecked = { model.changeListeningOptions(p.copy(skipTables = it)) }) }
        item { SwitchRow(Icons.Rounded.Functions, "Rumus", "Contoh: f = v / λ",
            checked = p.skipFormulas, onChecked = { model.changeListeningOptions(p.copy(skipFormulas = it)) }) }
        item { SwitchRow(Icons.Rounded.FormatQuote, "Kutipan referensi", "Contoh: (Penulis, 1995)",
            checked = p.skipCitations, onChecked = { model.changeListeningOptions(p.copy(skipCitations = it)) }) }
        item { SwitchRow(Icons.Rounded.Link, "URL", "Tautan halaman web",
            checked = p.skipUrls, onChecked = { model.changeListeningOptions(p.copy(skipUrls = it)) }) }
        item { HorizontalDivider(Modifier.padding(horizontal = 24.dp), color = Color(0xFF404040)) }
        item { SwitchRow(Icons.Rounded.DataObject, "Tanda kurung ( )", "Teks di dalam kurung biasa",
            checked = p.skipRound, onChecked = { model.changeListeningOptions(p.copy(skipRound = it)) }) }
        item { SwitchRow(Icons.Rounded.DataArray, "Tanda kurung [ ]", "Teks di dalam kurung siku",
            checked = p.skipSquare, onChecked = { model.changeListeningOptions(p.copy(skipSquare = it)) }) }
        item { SwitchRow(Icons.Rounded.Code, "Tanda kurung kurawal { }", "Teks di dalam kurung kurawal",
            checked = p.skipCurly, onChecked = { model.changeListeningOptions(p.copy(skipCurly = it)) }) }
        item {
            Text("Penyaringan berbasis pola teks, bukan AI. Teks asli dokumen tidak diubah.",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                style = MaterialTheme.typography.bodySmall, color = SpeechifyMuted)
        }
    }
}

data class VoiceStyle(val title: String, val pitch: Float, val detail: String)
private val voiceStyles = listOf(
    VoiceStyle("Standar", 1f, "Nada asli"),
    VoiceStyle("Hangat", 0.94f, "Sedikit lebih rendah"),
    VoiceStyle("Dalam", 0.82f, "Nada rendah"),
    VoiceStyle("Ceria", 1.15f, "Nada lebih tinggi")
)

@Composable
fun VoiceLibraryV2(model: TuturiumModel, onBack: () -> Unit) {
    LaunchedEffect(Unit) { model.loadVoices() }
    Column(Modifier.fillMaxSize().background(SpeechifyPanel)) {
        Row(Modifier.fillMaxWidth().padding(top = 16.dp, start = 7.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Kembali") }
            Text("Pustaka Suara", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Icon(Icons.Rounded.WifiOff, "Mode offline", tint = SpeechifyMuted)
        }
        VoiceLibraryContent(model, modifier = Modifier.weight(1f))
    }
}

@Composable
fun VoiceLibraryContent(model: TuturiumModel, modifier: Modifier = Modifier) {
    var pendingOnlineVoice by remember { mutableStateOf<VoiceChoice?>(null) }
    val realVoices = model.voices.filter { isIndonesian(it.language) }
        .distinctBy { it.name }.sortedWith(compareBy<VoiceChoice> { it.network }
            .thenByDescending { it.quality })
    val primary = realVoices.firstOrNull { !it.network }
        ?: model.voices.firstOrNull { !it.network }
    LazyColumn(modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Suara Indonesia tersedia", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("Suara dari mesin Android, offline maupun online jika disediakan.",
                color = SpeechifyMuted, style = MaterialTheme.typography.bodySmall)
        }
        if (realVoices.isEmpty()) item {
            Surface(color = Color(0xFF333333), shape = RoundedCornerShape(18.dp)) {
                Text(model.voiceStatus.ifBlank {
                    "Belum tersedia suara Indonesia di mesin TTS perangkat. " +
                    "Profil nada memerlukan minimal satu suara offline."
                }, modifier = Modifier.padding(16.dp), color = SpeechifyMuted)
            }
        }
        items(realVoices.take(12), key = { it.name }) { voice ->
            Row(Modifier.fillMaxWidth().clickable {
                if (voice.network) pendingOnlineVoice = voice
                else model.changeSettings(model.settings.copy(voice = voice.name, pitch = 1f))
            }.padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                IndonesianVoiceAvatar(Modifier.size(55.dp))
                Spacer(Modifier.width(13.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (voice.name == model.settings.voice) "Terpilih" else voice.label,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(if (voice.network) "ID · Online melalui mesin Android"
                        else "ID · Offline", color = SpeechifyMuted,
                        style = MaterialTheme.typography.bodySmall)
                }
                if (voice.name == model.settings.voice) Icon(Icons.Rounded.CheckCircle,
                    "Dipilih", tint = SpeechifyAccent)
                else Icon(Icons.Rounded.ChevronRight, null, tint = SpeechifyMuted)
            }
        }
        item {
            Spacer(Modifier.height(12.dp))
            Text("4 Profil Suara", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(5.dp))
            Text("Empat variasi nada dari satu suara offline, " +
                "bukan empat model neural yang berbeda.",
                color = SpeechifyMuted, style = MaterialTheme.typography.bodySmall)
        }
        items(voiceStyles) { profile ->
            val chosen = primary != null && (model.settings.voice == primary.name ||
                (model.settings.voice == null && profile.pitch == 1f)) &&
                kotlin.math.abs(model.settings.pitch - profile.pitch) < 0.02f
            Row(Modifier.fillMaxWidth().clickable(enabled = primary != null) {
                val voice = realVoices.firstOrNull { it.name == model.settings.voice } ?: primary
                model.changeSettings(model.settings.copy(voice = voice?.name, pitch = profile.pitch))
            }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(55.dp).clip(CircleShape).background(
                    when (profile.title) {
                        "Hangat" -> Color(0xFF5B4A46)
                        "Dalam" -> Color(0xFF3D4D64)
                        "Ceria" -> Color(0xFF636048)
                        else -> Color(0xFF39435D)
                    }), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.RecordVoiceOver, null, tint = Color.White)
                }
                Spacer(Modifier.width(13.dp))
                Column(Modifier.weight(1f)) {
                    Text(profile.title)
                    Text(profile.detail, color = SpeechifyMuted, style = MaterialTheme.typography.bodySmall)
                }
                if (chosen) Icon(Icons.Rounded.CheckCircle, "Dipilih", tint = SpeechifyAccent)
                else Icon(Icons.Rounded.ChevronRight, null, tint = SpeechifyMuted)
            }
        }
        item {
            Spacer(Modifier.height(18.dp))
            Text("Suara neural online Speechify tidak tersedia sebagai API gratis tanpa batas " +
                "untuk digunakan oleh aplikasi lain. Tuturium tidak memakai koneksi tidak resmi.",
                color = SpeechifyMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
    pendingOnlineVoice?.let { voice ->
        AlertDialog(
            onDismissRequest = { pendingOnlineVoice = null },
            title = { Text("Gunakan suara online?") },
            text = { Text("Saat memakai suara ini, teks dokumen yang dibacakan dapat " +
                "dikirim ke penyedia mesin TTS Android. Memerlukan koneksi internet dan " +
                "ketersediaannya ditentukan penyedia, bukan Tuturium.") },
            confirmButton = { TextButton(onClick = {
                model.changeSettings(model.settings.copy(voice = voice.name, pitch = 1f))
                pendingOnlineVoice = null
            }) { Text("Gunakan suara") } },
            dismissButton = { TextButton(onClick = { pendingOnlineVoice = null }) { Text("Batal") } }
        )
    }
}

@Composable
fun IndonesianVoiceAvatar(modifier: Modifier = Modifier) {
    Column(modifier.clip(CircleShape)) {
        Box(Modifier.fillMaxWidth().weight(1f).background(Color(0xFFCF082B)))
        Box(Modifier.fillMaxWidth().weight(1f).background(Color.White))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedControlSheet(model: TuturiumModel, playback: Playback, dismiss: () -> Unit) {
    val p = model.listeningOptions
    val speed = model.settings.speed
    ModalBottomSheet(onDismissRequest = dismiss, containerColor = SpeechifyPanel) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Normal", style = MaterialTheme.typography.titleLarge)
            Text(if (p.autoRamp) "Naik setiap 500 kata · maksimal 4×"
                else "Kecepatan pemutaran", color = SpeechifyMuted,
                style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    model.changeSettings(model.settings.copy(speed =
                        (round((speed - 0.1f).coerceAtLeast(0.5f) * 10f) / 10f)))
                }, enabled = speed > 0.5f) { Icon(Icons.Rounded.Remove, "Perlambat") }
                Text(String.format(Locale.forLanguageTag("id-ID"), "%.1f×",
                    if (p.autoRamp && playback.playing) playback.effectiveSpeed else speed),
                    style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = {
                    model.changeSettings(model.settings.copy(speed =
                        (round((speed + 0.1f).coerceAtMost(4f) * 10f) / 10f)))
                }, enabled = speed < 4f) { Icon(Icons.Rounded.Add, "Percepat") }
            }
            Slider(value = speed, onValueChange = {
                model.changeSettings(model.settings.copy(speed = round(it * 10f) / 10f), persist = false)
            }, onValueChangeFinished = { model.changeSettings(model.settings) },
            valueRange = 0.5f..4f, colors = SliderDefaults.colors(
                thumbColor = SpeechifyAccent, activeTrackColor = SpeechifyAccent))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf(0.8f, 1f, 1.2f, 2f, 3f, 4f).forEach { x ->
                    TextButton(onClick = { model.changeSettings(model.settings.copy(speed = x)) }) {
                        Text(String.format(Locale.US, "%.1f×", x),
                            color = if (kotlin.math.abs(speed - x) < 0.05f) SpeechifyAccent else Color.White,
                            style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            HorizontalDivider(color = Color(0xFF454545))
            Row(Modifier.fillMaxWidth().clickable {
                model.changeListeningOptions(p.copy(autoRamp = !p.autoRamp))
            }, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Tingkatkan Kecepatan Otomatis",
                        style = MaterialTheme.typography.bodyLarge)
                    Text("+0,1× setiap 500 kata yang selesai didengarkan",
                        color = SpeechifyMuted, style = MaterialTheme.typography.bodySmall)
                }
                Switch(p.autoRamp,
                    onCheckedChange = { model.changeListeningOptions(p.copy(autoRamp = it)) },
                    colors = SwitchDefaults.colors(checkedTrackColor = SpeechifyAccent))
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkipOptionsSheet(model: TuturiumModel, dismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = dismiss, containerColor = SpeechifyPanel) {
        Box(Modifier.fillMaxWidth().heightIn(max = 690.dp)) {
            ListeningSettingsV2(model, onBack = dismiss)
        }
    }
}
