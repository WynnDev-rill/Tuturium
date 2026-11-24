package id.tuturium.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
    val volume = model.settings.volume.coerceIn(0.01f, 1f)
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
                valueRange = 0.01f..1f,
                onValueChangeFinished = { model.changeSettings(model.settings) })
            Text("100% menggunakan level narasi penuh. Volume media tetap diatur oleh perangkat.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private val readerThemes = listOf("system" to "Ikuti perangkat", "light" to "Terang", "dark" to "Gelap")
private val readerFonts = listOf(
    "sans" to "Sans sistem", "serif" to "Serif sistem", "mono" to "Monospace",
    "crimson" to "Crimson Pro", "comic" to "Comic Neue"
)
private val crimsonReaderFont = FontFamily(Font(R.font.crimson_pro))

internal fun readerTypeface(value: String): FontFamily = when (value) {
    "serif" -> FontFamily.Serif
    "mono" -> FontFamily.Monospace
    "crimson" -> crimsonReaderFont
    "comic" -> ReaderFont
    else -> FontFamily.SansSerif
}

private val cursorColors = listOf(
    "theme" to "Tuturium", "plum" to "Ungu", "red" to "Merah", "amber" to "Jingga", "mint" to "Mint"
)

internal fun readerCursorColor(value: String, brand: Color): Color = when (value) {
    "plum" -> Color(0xFFE5A9F4)
    "red" -> Color(0xFFFFADAC)
    "amber" -> Color(0xFFFFCC73)
    "mint" -> Color(0xFF94DED0)
    else -> brand
}

@Composable
internal fun ReaderAppearanceSheet(model: TuturiumModel, dismiss: () -> Unit) {
    var page by remember { mutableStateOf("Tampilan") }
    var themeMenu by remember { mutableStateOf(false) }
    TuturiumSheet(onDismissRequest = dismiss, containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (page != "Tampilan") {
                    IconButton(onClick = { page = "Tampilan" }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Kembali ke Tampilan")
                    }
                }
                Text(page, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                if (page == "Tampilan") TextButton(onClick = dismiss) { Text("Selesai") }
            }
            when (page) {
                "Warna sorotan" -> {
                    Row(Modifier.fillMaxWidth().selectableGroup().padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for ((value, label) in cursorColors) {
                            val selected = model.readerHighlight == value
                            val color = readerCursorColor(value, MaterialTheme.colorScheme.primary)
                            Surface(Modifier.weight(1f).height(52.dp)
                                .selectable(selected, role = Role.RadioButton) { model.changeReaderHighlight(value) }
                                .semantics { contentDescription = label },
                                shape = RoundedCornerShape(14.dp), color = color,
                                contentColor = if (value == "theme") MaterialTheme.colorScheme.onPrimary else Color(0xFF202020),
                                border = if (selected) BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface) else null) {
                                Box(contentAlignment = Alignment.Center) { Text("Aa") }
                            }
                        }
                    }
                    AppearanceSwitch("Sorot kalimat", model.readerHighlightSentence, model::changeReaderHighlightSentence)
                }
                "Font" -> {
                    Column(Modifier.selectableGroup()) {
                        for ((value, label) in readerFonts) {
                            val selected = model.readerFont == value
                            Row(Modifier.fillMaxWidth().heightIn(min = 54.dp)
                                .selectable(selected, role = Role.RadioButton) { model.changeReaderFont(value) }
                                .padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(label, Modifier.weight(1f), fontFamily = readerTypeface(value), fontSize = 18.sp)
                                if (selected) Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                else -> {
                    Box {
                        AppearanceChoice("Tema pemutar",
                            readerThemes.firstOrNull { it.first == model.theme }?.second ?: "Ikuti perangkat") {
                            themeMenu = true
                        }
                        TuturiumMenu(expanded = themeMenu, onDismissRequest = { themeMenu = false }) {
                            for ((value, label) in readerThemes) {
                                DropdownMenuItem(text = { Text(label) }, onClick = {
                                    model.changeTheme(value)
                                    themeMenu = false
                                }, trailingIcon = { if (model.theme == value) Icon(Icons.Rounded.Check, "Terpilih") })
                            }
                        }
                    }
                    AppearanceChoice("Warna sorotan",
                        cursorColors.firstOrNull { it.first == model.readerHighlight }?.second ?: "Tuturium"
                    ) { page = "Warna sorotan" }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth().selectableGroup().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for ((value, label) in listOf("standard" to "Standar", "warm" to "Sepia", "focus" to "Fokus")) {
                            val selected = model.readerPreset == value
                            Surface(Modifier.weight(1f).heightIn(min = 64.dp)
                                .selectable(selected, role = Role.RadioButton) { model.changeReaderPreset(value) },
                                shape = RoundedCornerShape(16.dp),
                                color = if (selected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainer,
                                border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null) {
                                Column(Modifier.padding(vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Aa", fontFamily = if (value == "warm") FontFamily.Serif else FontFamily.SansSerif)
                                    Text(label, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    AppearanceChoice("Font", readerFonts.firstOrNull { it.first == model.readerFont }?.second ?: "Sans sistem") {
                        page = "Font"
                    }
                    Row(Modifier.fillMaxWidth().padding(start = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Ukuran font", Modifier.weight(1f))
                        OutlinedIconButton(enabled = model.fontSize > 16f,
                            onClick = { model.changeFont((model.fontSize - 1).coerceAtLeast(16f)) }) {
                            Icon(Icons.Rounded.Remove, "Perkecil font")
                        }
                        Text("${model.fontSize.toInt()} sp", Modifier.padding(horizontal = 6.dp))
                        OutlinedIconButton(enabled = model.fontSize < 34f,
                            onClick = { model.changeFont((model.fontSize + 1).coerceAtMost(34f)) }) {
                            Icon(Icons.Rounded.Add, "Perbesar font")
                        }
                    }
                    AppearanceSwitch("Teks tebal", model.readerBold, model::changeReaderBold)
                }
            }
        }
    }
}

@Composable
private fun AppearanceChoice(label: String, value: String, action: () -> Unit) {
    Surface(onClick = action, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        color = Color.Transparent, shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, Modifier.weight(1f))
            Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Icon(Icons.Rounded.ChevronRight, null, Modifier.padding(start = 4.dp))
        }
    }
}

@Composable
private fun AppearanceSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        Switch(checked, onChange, Modifier.semantics { contentDescription = label })
    }
}
