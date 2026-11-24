package id.tuturium.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

internal data class ReaderMatch(val row: Int, val range: IntRange)

internal fun findReaderMatches(rows: List<String>, query: String): List<ReaderMatch> {
    val term = query.trim()
    if (term.isEmpty()) return emptyList()
    return buildList {
        rows.forEachIndexed { row, text ->
            var start = text.indexOf(term, ignoreCase = true)
            while (start >= 0) {
                val end = start + term.length
                add(ReaderMatch(row, start until end))
                start = text.indexOf(term, startIndex = end, ignoreCase = true)
            }
        }
    }
}

@Composable
internal fun ReaderSearchBar(
    query: String, onQuery: (String) -> Unit, selected: Int, count: Int,
    onNavigate: (Int) -> Unit, onClose: () -> Unit
) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    Surface(Modifier.fillMaxWidth().imePadding().padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(25.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(Modifier.padding(horizontal = 3.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(enabled = count > 1, onClick = { onNavigate(-1) }) {
                Icon(Icons.Rounded.KeyboardArrowUp, "Hasil sebelumnya")
            }
            IconButton(enabled = count > 1, onClick = { onNavigate(1) }) {
                Icon(Icons.Rounded.KeyboardArrowDown, "Hasil berikutnya")
            }
            val ink = MaterialTheme.colorScheme.onSurface
            BasicTextField(query, onQuery, Modifier.weight(1f).focusRequester(focus)
                .semantics { contentDescription = "Cari di dokumen" }, singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = ink),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { input -> Box {
                    if (query.isEmpty()) Text("Cari teks", color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium)
                    input()
                } })
            if (query.isNotBlank()) {
                Text(if (count == 0) "0/0" else "${selected + 1}/$count",
                    style = MaterialTheme.typography.labelMedium)
                IconButton(onClick = { onQuery("") }) { Icon(Icons.Rounded.Clear, "Hapus pencarian") }
            }
            IconButton(onClick = onClose) { Icon(Icons.Rounded.Done, "Tutup pencarian") }
        }
    }
}

@Composable
internal fun RenameBookSheet(title: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var value by remember(title) { mutableStateOf(TextFieldValue(title, TextRange(0, title.length))) }
    TuturiumSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
        val focus = remember { FocusRequester() }
        LaunchedEffect(Unit) { focus.requestFocus() }
        Column(Modifier.fillMaxWidth().imePadding().padding(start = 22.dp, end = 22.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)) {
            Text("Ubah nama", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(value, { if (it.text.length <= 200) value = it },
                modifier = Modifier.fillMaxWidth().focusRequester(focus), singleLine = true,
                label = { Text("Nama") })
            Button(onClick = { onSave(value.text.trim()) }, enabled = value.text.isNotBlank(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)) {
                Text("Simpan")
            }
        }
    }
}

@Composable
internal fun ReaderAudioSheet(model: TuturiumModel, dismiss: () -> Unit) {
    val volume = model.settings.volume
    val options = model.listeningOptions
    TuturiumSheet(onDismissRequest = dismiss, containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Column(Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Audio", style = MaterialTheme.typography.titleLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Padukan dengan musik latar", Modifier.weight(1f))
                Switch(options.mixWithMusic, { model.changeListeningOptions(options.copy(mixWithMusic = it)) })
            }
            Text("Volume narasi ${(volume * 100).toInt()}%", style = MaterialTheme.typography.titleMedium)
            Slider(volume, { model.changeSettings(model.settings.copy(volume = it), persist = false) },
                onValueChangeFinished = { model.changeSettings(model.settings) })
        }
    }
}

@Composable
internal fun ReaderAppearanceSheet(model: TuturiumModel, dismiss: () -> Unit) {
    TuturiumSheet(onDismissRequest = dismiss, containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Column(Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Text("Tampilan", style = MaterialTheme.typography.titleLarge)
            Text("Tema")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("system" to "Sistem", "light" to "Terang", "dark" to "Gelap").forEach { (value, label) ->
                    FilterChip(model.theme == value, { model.changeTheme(value) }, label = { Text(label) })
                }
            }
            Text("Font")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("sans" to "Sans", "serif" to "Serif", "mono" to "Mono").forEach { (value, label) ->
                    FilterChip(model.readerFont == value, { model.changeReaderFont(value) }, label = { Text(label) })
                }
            }
            Text("Sorotan")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("theme" to "Tuturium", "amber" to "Jingga", "mint" to "Mint").forEach { (value, label) ->
                    FilterChip(model.readerHighlight == value, { model.changeReaderHighlight(value) },
                        label = { Text(label) })
                }
            }
            VoiceSlider("Ukuran teks", "${model.fontSize.toInt()} sp", model.fontSize, 16f..34f, model::changeFont)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Teks tebal", Modifier.weight(1f))
                Switch(model.readerBold, model::changeReaderBold)
            }
            TextButton(onClick = dismiss, modifier = Modifier.align(Alignment.End)) { Text("Selesai") }
        }
    }
}
