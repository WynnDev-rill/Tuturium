package id.tuturium.app

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.key.*
import kotlin.math.roundToInt

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
        item { SwitchRow(Icons.Rounded.AutoAwesomeMotion, "Gulir otomatis",
            checked = p.autoScroll, onChecked = { model.changeListeningOptions(p.copy(autoScroll = it)) }) }
        item { SwitchRow(Icons.Rounded.Translate, "Cocokkan Suara dengan Bahasa",
            "Prioritaskan Indonesia. Jika nonaktif, coba suara luring sesuai bahasa perangkat.",
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
    OfflineCatalog.voice(voice.name) != null -> "Supertonic 3 · Offline"
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
    LaunchedEffect(model) { model.refreshOfflineModels() }
    val google = googleIndonesianVoices(model.voices)
    val pack = OfflineCatalog.models.single()
    val installed = pack.id in model.installedModels
    val supertonic = if (installed) pack.voices.map {
        VoiceChoice(it.id, "id", it.label, quality = 0)
    } else emptyList()
    val voices = google + supertonic
    val selected = voices.firstOrNull { it.name == model.settings.voice }
        ?: resolveIndonesianVoice(google, model.settings.voice)
    var openedName by rememberSaveable { mutableStateOf<String?>(null) }
    val opened = voices.firstOrNull { it.name == openedName }
    val closeDetails = {
        model.stopVoicePreview()
        openedName = null
    }
    BackHandler(opened != null, onBack = closeDetails)
    DisposableEffect(model) { onDispose { model.stopVoicePreview() } }
    if (opened != null) {
        VoiceDetailPanel(model, opened, google, modifier, closeDetails)
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
                        Text(voiceLabel(voice, google),
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
        if (google.isEmpty()) item {
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
        } else items(google, key = { it.name }) { voice ->
            VoiceLibraryRow(voice, google, selected?.name == voice.name) { openedName = voice.name }
        }
        item {
            Spacer(Modifier.height(14.dp))
            Text("Supertonic 3", style = MaterialTheme.typography.titleLarge)
            Text("Suara bahasa Indonesia di perangkat",
                color = SpeechifyMuted, style = MaterialTheme.typography.bodySmall)
        }
        if (installed) items(supertonic, key = { it.name }) { voice ->
            VoiceLibraryRow(voice, google, selected?.name == voice.name) { openedName = voice.name }
        } else item { OfflineModelCard(model, pack) }
        if (model.voiceStatus.isNotBlank() && voices.isNotEmpty()) item {
            Text(model.voiceStatus, style = MaterialTheme.typography.bodySmall, color = SpeechifyMuted)
        }
    }
}

private fun voiceLabel(voice: VoiceChoice, google: List<VoiceChoice>): String =
    OfflineCatalog.voice(voice.name)?.label ?: indonesianVoiceLabel(voice, google)

@Composable
private fun VoiceLibraryRow(voice: VoiceChoice, google: List<VoiceChoice>, selected: Boolean,
    openDetails: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = openDetails)
        .heightIn(min = 83.dp).padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically) {
        IndonesianVoiceAvatar(Modifier.size(61.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(voiceLabel(voice, google), style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(4.dp))
            Text(voiceSubtitle(voice), color = SpeechifyMuted, style = MaterialTheme.typography.bodySmall)
        }
        if (selected) Icon(Icons.Rounded.CheckCircle, "Terpilih",
            tint = SpeechifyAccent, modifier = Modifier.size(23.dp))
        else Icon(Icons.Rounded.ChevronRight, "Buka detail suara", tint = SpeechifyMuted)
    }
}

@Composable
private fun VoiceDetailPanel(model: TuturiumModel, voice: VoiceChoice, all: List<VoiceChoice>,
    modifier: Modifier, onBack: () -> Unit) {
    val name = voiceLabel(voice, all)
    val selected = model.settings.voice == voice.name ||
        (model.settings.voice == null &&
            resolveIndonesianVoice(all, null)?.name == voice.name)
    val playing = model.previewingVoice == voice.name
    val favorite = voice.name in model.favoriteVoices
    Column(modifier.fillMaxWidth().verticalScroll(rememberScrollState())
        .padding(horizontal = 23.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Kembali ke pustaka suara") }
            Spacer(Modifier.weight(1f))
            Text(voiceSubtitle(voice),
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

@Composable
internal fun SpeedControlSheet(model: TuturiumModel, playback: Playback, book: Book,
    position: ReadingPosition?, dismiss: () -> Unit) {
    val options = model.listeningOptions
    var dragSpeed by remember { mutableStateOf<Float?>(null) }
    val speed = dragSpeed ?: playback.estimatedReadingSpeed(book, model.settings.speed)
    val seconds = position?.remainingSeconds(narrationMillisPerWord(model, speed, model.readerPace), speed)
    val colors = MaterialTheme.colorScheme
    fun setSpeed(value: Float) {
        val rounded = (value * 20).roundToInt() / 20f
        model.choosePlaybackSpeed(rounded.coerceIn(PLAYBACK_SPEED_RANGE))
    }

    TuturiumSheet(onDismissRequest = dismiss, containerColor = colors.surface) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f).heightIn(min = 268.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(when {
                            speed <= 0.9f -> "Lambat"
                            speed <= 1.5f -> "Normal"
                            speed <= 2.5f -> "Cepat"
                            else -> "Sangat cepat"
                        }, style = MaterialTheme.typography.titleLarge)
                        Text(if (seconds == null) "Menghitung durasi…"
                            else "≈${formatPlaybackTime(seconds)} tersisa",
                            color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { setSpeed(speed - SPEED_STEP) },
                            enabled = speed > PLAYBACK_SPEED_RANGE.start) {
                            Icon(Icons.Rounded.Remove, "Perlambat")
                        }
                        Text(formatPlaybackSpeed(speed), style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold, color = colors.onSurface)
                        IconButton(onClick = { setSpeed(speed + SPEED_STEP) },
                            enabled = speed < PLAYBACK_SPEED_RANGE.endInclusive) {
                            Icon(Icons.Rounded.Add, "Percepat")
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        for (preset in listOf(0.8f, 1f, 1.2f)) {
                            TextButton(onClick = { setSpeed(preset) }, modifier = Modifier.semantics {
                                contentDescription = "Pilih ${formatPlaybackSpeed(preset)}"
                            }) {
                                Text(formatPlaybackSpeed(preset), color = colors.onSurface)
                            }
                        }
                    }
                }
                VerticalSpeedSlider(speed, onPreview = { dragSpeed = it }, onCommit = {
                    setSpeed(it)
                    dragSpeed = null
                })
            }
            Row(Modifier.fillMaxWidth().heightIn(min = 52.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Tingkatkan Kecepatan Otomatis", style = MaterialTheme.typography.titleSmall)
                    Text("+0,05× tiap $AUTO_SPEED_WORDS kata · hingga 4×",
                        color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                Switch(options.autoRamp,
                    onCheckedChange = { model.changeListeningOptions(options.copy(autoRamp = it)) },
                    modifier = Modifier.semantics { contentDescription = "Tingkatkan kecepatan otomatis" })
            }
        }
    }
}

@Composable
private fun VerticalSpeedSlider(value: Float, onPreview: (Float?) -> Unit, onCommit: (Float) -> Unit) {
    val range = PLAYBACK_SPEED_RANGE
    val bounded = value.coerceIn(range)
    val fraction = (bounded - range.start) / (range.endInclusive - range.start)
    val preview by rememberUpdatedState(onPreview)
    val commit by rememberUpdatedState(onCommit)
    val colors = MaterialTheme.colorScheme
    Box(Modifier.size(58.dp, 268.dp)
        .clip(RoundedCornerShape(22.dp)).background(colors.surface)
        .semantics {
            contentDescription = "Penggeser kecepatan"
            progressBarRangeInfo = ProgressBarRangeInfo(bounded, range, 69)
            setProgress {
                onCommit(it)
                true
            }
        }
        .onKeyEvent {
            if (it.type != KeyEventType.KeyDown) return@onKeyEvent false
            val change = when (it.key) {
                Key.DirectionUp, Key.DirectionRight -> SPEED_STEP
                Key.DirectionDown, Key.DirectionLeft -> -SPEED_STEP
                else -> return@onKeyEvent false
            }
            onCommit(bounded + change)
            true
        }.focusable()
        .pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown()
                down.consume()
                fun speedAt(y: Float): Float {
                    val fractionUp = (1f - y / size.height).coerceIn(0f, 1f)
                    return ((range.start + fractionUp * (range.endInclusive - range.start)) * 20f)
                        .roundToInt() / 20f
                }
                var selected = speedAt(down.position.y)
                var completed = false
                try {
                    preview(selected)
                    while (true) {
                        val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                        if (change.isConsumed) break
                        selected = speedAt(change.position.y)
                        change.consume()
                        if (!change.pressed) {
                            completed = true
                            break
                        }
                        preview(selected)
                    }
                } finally {
                    if (completed) commit(selected) else preview(null)
                }
            }
        }.padding(8.dp)) {
        BoxWithConstraints(Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceContainerHighest)) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally) {
                repeat(11) { i -> Box(Modifier.width(if (i % 3 == 0) 30.dp else 18.dp)
                    .height(2.dp).background(colors.outlineVariant)) }
            }
            Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(fraction)
                .background(colors.primary))
            Box(Modifier.align(Alignment.BottomCenter)
                .offset(y = -(maxHeight - 7.dp) * fraction).width(26.dp).height(7.dp)
                .clip(CircleShape).background(colors.onPrimary))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkipOptionsSheet(model: TuturiumModel, dismiss: () -> Unit) {
    TuturiumSheet(onDismissRequest = dismiss, containerColor = SpeechifyPanel) {
        Box(Modifier.fillMaxWidth().heightIn(max = 690.dp)) {
            ListeningSettingsV2(model, onBack = dismiss)
        }
    }
}

@Composable
private fun OfflineModelCard(model: TuturiumModel, pack: OfflineModel) {
    val downloading = model.downloadingModel == pack.id
    Surface(color = Color(0xFF252525), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Unduh paket suara", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text("${pack.bytes / 1_000_000L} MB · ${pack.voices.size} suara",
                        color = SpeechifyMuted, style = MaterialTheme.typography.bodySmall)
                }
                if (!downloading) IconButton(onClick = { model.downloadVoiceModel(pack.id) },
                    enabled = model.downloadingModel == null) { Icon(Icons.Rounded.Download, "Unduh Supertonic 3") }
                if (downloading) IconButton(onClick = model::cancelVoiceDownload) {
                    Icon(Icons.Rounded.Close, "Batalkan unduhan")
                }
            }
            if (downloading) {
                val progress = model.modelDownloadProgress
                if (progress == null) LinearProgressIndicator(Modifier.fillMaxWidth())
                else {
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    Text("Mengunduh ${(progress * 100).toInt()}%", color = SpeechifyMuted)
                }
            }
            if (model.failedModelId == pack.id && !downloading) model.modelDownloadError?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
