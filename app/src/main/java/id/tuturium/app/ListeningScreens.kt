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

private fun voiceSubtitle(voice: VoiceChoice): String = when {
    voice.name == GOOGLE_ID_AUTO -> "Google TTS · Pilihan otomatis"
    voice.network -> "Google TTS · Online"
    else -> "Google TTS · Offline"
}

@Composable
fun VoiceLibraryV2(model: TuturiumModel, onBack: () -> Unit) {
    LaunchedEffect(Unit) { model.loadVoices() }
    Column(Modifier.fillMaxSize().background(SpeechifyPanel)) {
        Row(Modifier.fillMaxWidth().padding(top = 16.dp, start = 7.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Kembali") }
            Text("Pustaka Suara", style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f))
            Icon(Icons.Rounded.Wifi, "Mendukung suara online dan offline", tint = Color.White)
        }
        VoiceLibraryContent(model, modifier = Modifier.weight(1f))
    }
}

@Composable
fun VoiceLibraryContent(model: TuturiumModel, modifier: Modifier = Modifier) {
    val voices = googleIndonesianVoices(model.voices)
    val selected = resolveIndonesianVoice(voices, model.settings.voice)
    var openedName by remember { mutableStateOf<String?>(null) }
    val opened = voices.firstOrNull { it.name == openedName }
    DisposableEffect(model) { onDispose { model.stopVoicePreview() } }
    if (opened != null) {
        VoiceDetailPanel(model, opened, voices, modifier) { openedName = null }
        return
    }
    val shortcuts = (voices.filter { it.name in model.favoriteVoices } +
        listOfNotNull(selected)).distinctBy { it.name }
    LazyColumn(modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 21.dp, vertical = 19.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Indonesia", "Gratis", "Online + offline").forEach { name ->
                    Surface(color = Color(0xFF313151), shape = RoundedCornerShape(27.dp)) {
                        Text(name, Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
                            color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(Modifier.height(21.dp))
            Text("Pintasan", style = MaterialTheme.typography.titleLarge)
        }
        if (shortcuts.isNotEmpty()) item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(17.dp)) {
                shortcuts.take(4).forEach { voice ->
                    Column(Modifier.clickable { openedName = voice.name },
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Box {
                            IndonesianVoiceAvatar(Modifier.size(64.dp))
                            if (voice.name == selected?.name)
                                Icon(Icons.Rounded.CheckCircle, "Suara terpilih",
                                    Modifier.align(Alignment.BottomEnd).size(24.dp),
                                    tint = SpeechifyAccent)
                        }
                        Spacer(Modifier.height(7.dp))
                        Text(indonesianVoiceLabel(voice, voices),
                            maxLines = 1, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        item {
            Spacer(Modifier.height(16.dp))
            Text("Indonesia", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text("Suara Google Speech Services berbahasa Indonesia",
                color = SpeechifyMuted, style = MaterialTheme.typography.bodySmall)
        }
        if (voices.isEmpty()) item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(model.voiceStatus.ifBlank {
                    "Tidak ada suara Indonesia yang dilaporkan oleh Google TTS."
                }, color = SpeechifyMuted)
                Button(onClick = model::installGoogleVoices,
                    colors = ButtonDefaults.buttonColors(containerColor = SpeechifyAccent)) {
                    Icon(Icons.Rounded.Download, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Pasang suara Google Indonesia")
                }
                OutlinedButton(onClick = model::loadVoices) {
                    Icon(Icons.Rounded.Refresh, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Periksa suara lagi")
                }
            }
        } else items(voices, key = { it.name }) { voice ->
            val active = selected?.name == voice.name
            Row(Modifier.fillMaxWidth().clickable { openedName = voice.name }
                .heightIn(min = 83.dp).padding(vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically) {
                IndonesianVoiceAvatar(Modifier.size(61.dp))
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(indonesianVoiceLabel(voice, voices),
                        style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(voiceSubtitle(voice), color = SpeechifyMuted,
                        style = MaterialTheme.typography.bodySmall)
                }
                if (active) Icon(Icons.Rounded.CheckCircle, "Terpilih",
                    tint = SpeechifyAccent, modifier = Modifier.size(23.dp))
                else Icon(Icons.Rounded.ChevronRight, "Buka detail suara",
                    tint = SpeechifyMuted)
            }
        }
        if (model.voiceStatus.isNotBlank() && voices.isNotEmpty()) item {
            Text(model.voiceStatus, style = MaterialTheme.typography.bodySmall, color = SpeechifyMuted)
        }
        item {
            Spacer(Modifier.height(14.dp))
            Text("Suara online memerlukan internet. Daftar ini berasal dari mesin " +
                "Google TTS pada perangkat, bukan daftar suara milik Speechify. " +
                "Gunakan pratinjau untuk memeriksa apakah setiap suara dapat diputar.",
                color = SpeechifyMuted, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = model::loadVoices) {
                Icon(Icons.Rounded.Refresh, null)
                Spacer(Modifier.width(7.dp))
                Text("Muat ulang suara")
            }
        }
    }
}

@Composable
private fun VoiceDetailPanel(model: TuturiumModel, voice: VoiceChoice, all: List<VoiceChoice>,
    modifier: Modifier, onBack: () -> Unit) {
    val name = indonesianVoiceLabel(voice, all)
    val selected = model.settings.voice == voice.name ||
        (model.settings.voice == null &&
            resolveIndonesianVoice(all, null)?.name == voice.name)
    val playing = model.previewingVoice == voice.name
    val favorite = voice.name in model.favoriteVoices
    Column(modifier.fillMaxWidth().verticalScroll(rememberScrollState())
        .padding(horizontal = 23.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                model.stopVoicePreview()
                onBack()
            }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Kembali ke pustaka suara") }
            Spacer(Modifier.weight(1f))
            Text(if (voice.network && voice.name != GOOGLE_ID_AUTO) "Online" else
                if (voice.name == GOOGLE_ID_AUTO) "Google TTS" else "Offline",
                style = MaterialTheme.typography.labelMedium, color = SpeechifyMuted)
        }
        Spacer(Modifier.height(18.dp))
        IndonesianVoiceAvatar(Modifier.size(156.dp))
        Spacer(Modifier.height(18.dp))
        Text(name, style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Text("Bahasa Indonesia · " + when {
            voice.name == GOOGLE_ID_AUTO -> "Otomatis"
            voice.network -> "Memerlukan internet"
            else -> "Di perangkat"
        }, color = SpeechifyMuted, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(27.dp))
        Surface(onClick = { model.toggleFavoriteVoice(voice.name) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            color = Color(0xFF303030), shape = RoundedCornerShape(30.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {
                Icon(if (favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    null, tint = Color.White)
                Spacer(Modifier.width(11.dp))
                Text(if (favorite) "Hapus dari Favorit" else "Tambahkan ke Favorit",
                    style = MaterialTheme.typography.bodyLarge, color = Color.White)
            }
        }
        Spacer(Modifier.height(27.dp))
        Text("Pratinjau", modifier = Modifier.align(Alignment.Start),
            style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Surface(onClick = { model.previewVoice(voice.name) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 74.dp),
            shape = RoundedCornerShape(21.dp), color = Color(0xFF303030)) {
            Column {
                Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 7.dp, top = 5.dp, bottom = 5.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Description, null, Modifier.size(29.dp), tint = Color.White)
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Pratinjau suara", color = Color.White,
                            style = MaterialTheme.typography.bodyLarge)
                        Text(when {
                            playing && model.previewLoading -> "Menyiapkan suara…"
                            playing -> "Sedang diputar"
                            else -> "Ketuk untuk mendengarkan"
                        }, color = SpeechifyMuted, style = MaterialTheme.typography.bodySmall)
                    }
                    Box(Modifier.size(54.dp).clip(CircleShape).background(SpeechifyAccent),
                        contentAlignment = Alignment.Center) {
                        if (playing && model.previewLoading) {
                            CircularProgressIndicator(Modifier.size(23.dp),
                                color = Color.White, strokeWidth = 2.dp)
                        } else Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            if (playing) "Hentikan pratinjau" else "Putar pratinjau",
                            Modifier.size(30.dp), tint = Color.White)
                    }
                }
                if (playing) LinearProgressIndicator(modifier = Modifier.fillMaxWidth()
                    .height(2.dp), color = SpeechifyAccent)
            }
        }
        model.previewError?.let { message ->
            Spacer(Modifier.height(10.dp))
            Text(message, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(22.dp))
        Button(onClick = {
            model.stopVoicePreview()
            model.changeSettings(model.settings.copy(voice = voice.name, pitch = 1f))
        }, modifier = Modifier.fillMaxWidth().heightIn(min = 57.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SpeechifyAccent),
            shape = RoundedCornerShape(28.dp)) {
            Icon(if (selected) Icons.Rounded.CheckCircle else Icons.Rounded.Check, null)
            Spacer(Modifier.width(8.dp))
            Text(if (selected) "Suara terpilih" else "Pilih " + name,
                style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(Modifier.height(19.dp))
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
