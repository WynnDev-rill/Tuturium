package id.tuturium.app

import android.os.SystemClock
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderMenuSheet(model: TuturiumModel, book: Book, index: Int, marked: Boolean,
    allowPdfMargins: Boolean = false, dismiss: () -> Unit, navigate: (String) -> Unit) {
    val p = model.listeningOptions
    TuturiumSheet(onDismissRequest = dismiss, containerColor = SpeechifyPanel) {
        Column(Modifier.fillMaxWidth().heightIn(max = 740.dp).verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Description, null, Modifier.size(48.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(book.title, maxLines = 2, overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium)
                    Text("${book.words} kata · ${book.format.lowercase()}",
                        style = MaterialTheme.typography.bodySmall, color = SpeechifyMuted)
                }
                IconButton(onClick = { model.shareBook(book); dismiss() }) {
                    Icon(Icons.Rounded.Share, "Bagikan")
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(Triple("Cari", Icons.Rounded.Search, "search"),
                    Triple("Ubah Nama", Icons.Rounded.Edit, "rename"),
                    Triple("Tampilan", Icons.Rounded.TextFields, "display")).forEach { (label, icon, key) ->
                    Surface(onClick = { dismiss(); navigate(key) }, modifier = Modifier.weight(1f).height(91.dp),
                        color = Color(0xFF303030), shape = RoundedCornerShape(18.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center) {
                            Icon(icon, null, Modifier.size(27.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(label, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        }
                    }
                }
            }
            Spacer(Modifier.height(15.dp))
            ReaderMenuAction(Icons.Rounded.Bookmark,
                if (marked) "Hapus bookmark" else "Buat bookmark") {
                model.toggleBookmark(index); dismiss()
            }
            ReaderMenuAction(Icons.Rounded.BookmarkBorder, "Simpan", "Dokumen tersedia offline") {
                model.status = "Dokumen sudah tersimpan secara offline."; dismiss()
            }
            ReaderMenuAction(Icons.Rounded.Draw, "Catatan & Sorotan") { dismiss(); navigate("notes") }
            HorizontalDivider(color = Color.DarkGray)
            ReaderMenuAction(Icons.Rounded.QueueMusic, "Antrean") { dismiss(); navigate("queue") }
            HorizontalDivider(color = Color.DarkGray)
            ReaderMenuAction(Icons.Rounded.AutoMode, "Lewati Konten Secara Otomatis",
                "Header dan footer berulang, catatan kaki, kutipan") { dismiss(); navigate("skip") }
            if (allowPdfMargins) ReaderMenuAction(Icons.Rounded.CropFree, "Area bacaan PDF",
                "Atur margin untuk dibacakan") { dismiss(); navigate("pdfMargins") }
            ReaderMenuToggle(Icons.Rounded.AutoAwesomeMotion, "Gulir otomatis", p.autoScroll) {
                model.changeListeningOptions(p.copy(autoScroll = it))
            }
            ReaderMenuAction(Icons.Rounded.Bedtime, "Timer tidur") { dismiss(); navigate("timer") }
            ReaderMenuAction(Icons.Rounded.VolumeUp, "Audio", "Volume narasi dan musik latar") {
                dismiss(); navigate("audio")
            }
            HorizontalDivider(color = Color.DarkGray)
            ReaderMenuAction(Icons.Rounded.DeleteOutline, "Hapus file", danger = true) {
                dismiss(); navigate("delete")
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun ReaderMenuAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String,
    sub: String? = null, danger: Boolean = false, action: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 57.dp).clickable(onClick = action)
        .padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(26.dp), tint = if (danger) Color.Red else Color.White)
        Spacer(Modifier.width(15.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = if (danger) Color.Red else Color.White)
            if (sub != null) Text(sub, color = SpeechifyMuted,
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ReaderMenuToggle(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String,
    checked: Boolean, change: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 61.dp).clickable { change(!checked) },
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(25.dp))
        Spacer(Modifier.width(15.dp))
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked, onCheckedChange = change,
            colors = SwitchDefaults.colors(checkedTrackColor = SpeechifyAccent))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderVoiceSheet(model: TuturiumModel, dismiss: () -> Unit) {
    LaunchedEffect(Unit) { model.loadVoices() }
    TuturiumSheet(onDismissRequest = dismiss, containerColor = SpeechifyPanel) {
        Text("Pustaka Suara", modifier = Modifier.padding(start = 22.dp, top = 7.dp, bottom = 13.dp),
            style = MaterialTheme.typography.titleLarge)
        VoiceLibraryContent(model, modifier = Modifier.heightIn(max = 610.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderTimerSheet(playback: Playback, dismiss: () -> Unit) {
    val context = LocalContext.current
    val remaining = (playback.timerUntil - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
    TuturiumSheet(onDismissRequest = dismiss, containerColor = SpeechifyPanel) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(27.dp)) {
            Text("Timer tidur", style = MaterialTheme.typography.titleLarge)
            Text(if (remaining > 0) "${(remaining / 60_000) + 1} mnt" else "0:00",
                style = MaterialTheme.typography.displayLarge, color = SpeechifyMuted)
            listOf(listOf(5, 10, 15), listOf(30, 45, 60)).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    row.forEach { m ->
                        Surface(onClick = {
                            ReadingService.command(context, ReadingService.TIMER, minutes = m)
                            dismiss()
                        }, modifier = Modifier.weight(1f).height(53.dp),
                            color = Color(0xFF333333), shape = RoundedCornerShape(28.dp)) {
                            Box(contentAlignment = Alignment.Center) { Text("$m mnt") }
                        }
                    }
                }
            }
            if (remaining > 0L) TextButton(onClick = {
                ReadingService.command(context, ReadingService.TIMER, minutes = 0)
                dismiss()
            }) { Text("Matikan timer") }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderQueueSheet(model: TuturiumModel, dismiss: () -> Unit) {
    val context = LocalContext.current
    var revision by remember { mutableIntStateOf(0) }
    val queued = remember(revision) { BookActions.queue(context) }
    var adding by remember { mutableStateOf(false) }
    TuturiumSheet(onDismissRequest = dismiss, containerColor = SpeechifyPanel) {
        Column(Modifier.fillMaxWidth().heightIn(max = 660.dp).padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text("Antrian", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(15.dp))
            LazyColumn(Modifier.weight(1f, fill = false)) {
                if (queued.isEmpty()) item {
                    Text("Belum ada bacaan dalam antrean.", color = SpeechifyMuted,
                        modifier = Modifier.padding(vertical = 25.dp))
                }
                items(queued, key = { it }) { id ->
                    val book = model.books.firstOrNull { it.id == id }
                    if (book != null) Row(Modifier.fillMaxWidth().heightIn(min = 65.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Description, null)
                        Spacer(Modifier.width(12.dp))
                        Text(book.title, Modifier.weight(1f), maxLines = 2)
                        IconButton(onClick = {
                            BookActions.removeFromQueue(context, id)
                            revision++
                        }) { Icon(Icons.Rounded.Close, "Hapus dari antrean") }
                    }
                }
            }
            if (adding) LazyColumn(Modifier.heightIn(max = 210.dp)) {
                items(model.books.filterNot { it.id in queued }, key = { it.id }) { book ->
                    Row(Modifier.fillMaxWidth().clickable {
                        BookActions.enqueue(context, book.id)
                        revision++; adding = false
                    }.padding(vertical = 12.dp)) {
                        Icon(Icons.Rounded.Add, null)
                        Spacer(Modifier.width(12.dp))
                        Text(book.title, maxLines = 1)
                    }
                }
            }
            Spacer(Modifier.height(15.dp))
            Button(onClick = { adding = !adding },
                modifier = Modifier.fillMaxWidth().height(53.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF343434))) {
                Icon(Icons.Rounded.Add, null); Spacer(Modifier.width(10.dp)); Text("Tambahkan")
            }
            Spacer(Modifier.height(23.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderNotesSheet(book: Book, index: Int, dismiss: () -> Unit) {
    val context = LocalContext.current
    val library = remember(context) { Library.get(context) }
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    var value by remember(book.id, index) { mutableStateOf(library.note(book.id, index)) }
    TuturiumSheet(onDismissRequest = dismiss, containerColor = SpeechifyPanel) {
        Column(Modifier.fillMaxWidth().padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("Catatan & Sorotan", style = MaterialTheme.typography.titleLarge)
            Text("Kalimat ${index + 1}", color = SpeechifyMuted,
                style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(value, { value = it.take(3000) },
                modifier = Modifier.fillMaxWidth(), label = { Text("Catatan pribadi") }, minLines = 3)
            if (failed) Text("Catatan gagal disimpan. Periksa apakah dokumen masih tersedia.",
                color = MaterialTheme.colorScheme.error)
            Button(onClick = {
                val text = value
                saving = true
                failed = false
                scope.launch {
                    val saved = withContext(Dispatchers.IO) {
                        runCatching { library.saveNote(book.id, index, text) }.getOrDefault(false)
                    }
                    if (saved) dismiss() else failed = true
                    saving = false
                }
            }, modifier = Modifier.fillMaxWidth(), enabled = !saving,
                colors = ButtonDefaults.buttonColors(containerColor = SpeechifyAccent)) {
                Text("Simpan catatan")
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
