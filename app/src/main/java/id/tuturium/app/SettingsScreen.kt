package id.tuturium.app

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun SettingsScreen(model: TuturiumModel) {
    val context = LocalContext.current
    var voicePicker by remember { mutableStateOf(false) }
    val selected = chooseVoice(model.voices, model.settings.voice, Locale.getDefault().language)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 22.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Setelan", style = MaterialTheme.typography.displaySmall) }
        item {
            SettingCard("Suara offline", {
                Text("Suara dari Android dapat digunakan tanpa akun atau kuota Tuturium.",
                    style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onClick = { voicePicker = true }, enabled = model.voices.isNotEmpty()) {
                    Icon(Icons.Rounded.RecordVoiceOver, null)
                    Spacer(Modifier.width(8.dp))
                    val label = selected?.let { "${it.label} · ${it.name}" }
                    Text(if (model.settings.voice == null) "Otomatis${label?.let { " · $it" }.orEmpty()}"
                        else label ?: "Pilih suara")
                }
                if (model.voiceStatus.isNotBlank()) Text(model.voiceStatus, style = MaterialTheme.typography.bodySmall)
                VoiceSlider("Kecepatan", rate(model.settings.speed), model.settings.speed, .5f..3f,
                    { model.changeSettings(model.settings.copy(speed = it), false) }, { model.changeSettings(model.settings) })
                VoiceSlider("Nada", rate(model.settings.pitch), model.settings.pitch, .5f..2f,
                    { model.changeSettings(model.settings.copy(pitch = it), false) }, { model.changeSettings(model.settings) })
                TextButton(onClick = {
                    try {
                        context.startActivity(Intent("com.android.settings.TTS_SETTINGS"))
                    } catch (_: android.content.ActivityNotFoundException) {
                        context.startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
                    }
                }) { Text("Buka setelan TTS Android") }
                TextButton(model::loadVoices) { Text("Muat ulang daftar suara") }
            })
        }
        item {
            SettingCard("Tampilan pembaca", {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf("system" to "Sistem", "light" to "Terang", "dark" to "Gelap").forEach { (value, text) ->
                        FilterChip(model.theme == value, { model.changeTheme(value) }, { Text(text) })
                    }
                }
                VoiceSlider("Ukuran teks", "${model.fontSize.toInt()} sp", model.fontSize, 16f..34f, model::changeFont)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Ikuti teks yang dibacakan", Modifier.weight(1f))
                    Switch(model.follow, model::changeFollow)
                }
            })
        }
        item {
            SettingCard("Pembaruan aplikasi", {
                Text("Tuturium ${BuildConfig.VERSION_NAME}")
                model.updateStatus?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton({ model.checkUpdate() }, enabled = !model.checking && model.updateProgress == null) {
                        Text("Periksa pembaruan")
                    }
                    if (model.release != null) TextButton({ model.showUpdate = true }) { Text("Lihat rilis") }
                }
            })
        }
    }

    if (voicePicker) {
        AlertDialog(onDismissRequest = { voicePicker = false }, title = { Text("Pilih suara offline") }, text = {
            LazyColumn(Modifier.heightIn(max = 380.dp)) {
                item {
                    VoiceOption("Otomatis · prioritaskan Indonesia", null, model.settings.voice == null) {
                        model.changeSettings(model.settings.copy(voice = null))
                        voicePicker = false
                    }
                }
                items(model.voices, key = { it.name }) { voice ->
                    VoiceOption(voice.label, voice.name, model.settings.voice == voice.name) {
                        model.changeSettings(model.settings.copy(voice = voice.name))
                        voicePicker = false
                    }
                }
            }
        }, confirmButton = { TextButton({ voicePicker = false }) { Text("Tutup") } })
    }
}

@Composable
private fun VoiceOption(label: String, name: String?, selected: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick) {
        ListItem(headlineContent = { Text(label) }, supportingContent = name?.let { { Text(it) } },
            leadingContent = { RadioButton(selected, onClick = null) })
    }
}

@Composable
private fun SettingCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}

@Composable
fun VoiceSlider(label: String, valueLabel: String, value: Float, range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit, finished: (() -> Unit)? = null) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
            Text(valueLabel, style = MaterialTheme.typography.labelLarge)
        }
        Slider(value, onChange, valueRange = range, onValueChangeFinished = finished)
    }
}

@Composable
fun UpdateScreen(model: TuturiumModel, release: Release) {
    val context = LocalContext.current
    val progress = model.updateProgress
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton({ model.showUpdate = false }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Kembali") }
            Text("Pembaruan Tuturium", style = MaterialTheme.typography.titleMedium)
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(27.dp)) {
                    Column(Modifier.fillMaxWidth().padding(21.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Image(painterResource(R.drawable.brand), null, Modifier.size(62.dp))
                        Text("Pembaruan tersedia", style = MaterialTheme.typography.titleMedium)
                        Text(release.version, style = MaterialTheme.typography.displayMedium)
                        Text("Dari ${BuildConfig.VERSION_NAME} ke ${release.version} • ${release.date} • " +
                            String.format(Locale.forLanguageTag("id"), "%.1f MB", release.size / 1_000_000.0))
                    }
                }
            }
            if (release.notes.isNotBlank()) item {
                SettingCard("Catatan rilis", { Text(release.notes) })
            }
            item {
                SettingCard("Verifikasi APK", {
                    Text("Tuturium memeriksa checksum, versi, dan sertifikat APK sebelum membuka pemasang Android.")
                    model.updateStatus?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                })
            }
        }
        Surface(tonalElevation = 2.dp) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 13.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                if (progress != null) {
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    Text("${(progress * 100).toInt()}%")
                }
                Button(onClick = { model.downloadUpdate(context) }, enabled = progress == null,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                    Icon(Icons.Rounded.SystemUpdate, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (model.verifiedApk == null) "Unduh pembaruan" else "Pasang pembaruan")
                }
                TextButton({ model.showUpdate = false }) { Text("Nanti") }
            }
        }
    }
}
