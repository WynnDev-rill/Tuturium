package id.tuturium.app

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.io.File
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class LocalStorage(val documents: Long = 0, val database: Long = 0, val cache: Long = 0) {
    val total: Long get() = documents + database + cache
}

/** Count real local bytes, never pretend to have cloud backups. */
internal object StorageInspector {
    private fun bytes(dir: File): Long = if (!dir.exists()) 0L else
        dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }

    fun read(context: Context): LocalStorage {
        val database = context.getDatabasePath("library.db")
        val dbBytes = database.length() +
            File(database.parentFile, "library.db-wal").length() +
            File(database.parentFile, "library.db-shm").length()
        return LocalStorage(
            documents = bytes(File(context.filesDir, "books")),
            database = dbBytes,
            cache = bytes(File(context.cacheDir, "updates")) + bytes(File(context.cacheDir, "shared"))
        )
    }

    /** Never delete books, only generated update downloads and temporary TXT exports. */
    fun clearTemporary(context: Context) {
        for (name in listOf("updates", "shared")) {
            File(context.cacheDir, name).listFiles()?.forEach { it.deleteRecursively() }
        }
    }
}

internal fun humanSize(bytes: Long): String = when {
    bytes < 1024L -> "$bytes B"
    bytes < 1024L * 1024 -> String.format(Locale.forLanguageTag("id-ID"), "%.1f KB", bytes / 1024.0)
    else -> String.format(Locale.forLanguageTag("id-ID"), "%.1f MB", bytes / (1024.0 * 1024))
}

@Composable
fun StorageV2(model: TuturiumModel, onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var snapshot by remember { mutableStateOf(LocalStorage()) }
    var revision by remember { mutableIntStateOf(0) }
    var confirm by remember { mutableStateOf(false) }
    LaunchedEffect(revision, model.books, model.deletedBooks) {
        snapshot = withContext(Dispatchers.IO) { StorageInspector.read(context) }
    }
    Column(Modifier.fillMaxSize().background(Color.Black)) {
        Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 18.dp, bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Kembali") }
            Text("Penyimpanan & Data", Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.width(48.dp))
        }
        val cloud by TuturiumCloud.state.collectAsStateWithLifecycle()
        Text("Penyimpanan lokal", modifier = Modifier.padding(horizontal = 17.dp, vertical = 12.dp),
            style = MaterialTheme.typography.titleMedium)
        Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(22.dp), color = Color(0xFF202020)) {
            Column(Modifier.padding(horizontal = 21.dp, vertical = 23.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text("Total terpakai", style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFAAAAAA))
                Text(humanSize(snapshot.total), style = MaterialTheme.typography.headlineMedium)
                val share = if (snapshot.total == 0L) 0f
                    else (snapshot.documents.toFloat() / snapshot.total).coerceIn(0f, 1f)
                BoxWithConstraints(Modifier.fillMaxWidth().height(12.dp)
                    .background(Color(0xFF343434), RoundedCornerShape(20.dp))) {
                    Box(Modifier.width(maxWidth * share).fillMaxHeight()
                        .background(Color(0xFF29BC89), RoundedCornerShape(20.dp)))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Description, null, tint = Color(0xFF29BC89))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("File bacaan", style = MaterialTheme.typography.bodyLarge)
                        Text("${model.books.size} dokumen · ${model.deletedBooks.size} di sampah",
                            style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                    }
                    Text(humanSize(snapshot.documents), color = Color.LightGray)
                }
                HorizontalDivider(color = Color(0xFF3D3D3D))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Storage, null, tint = Color.LightGray)
                    Spacer(Modifier.width(10.dp))
                    Text("Database & progres", Modifier.weight(1f))
                    Text(humanSize(snapshot.database), color = Color.LightGray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.DownloadDone, null, tint = Color.LightGray)
                    Spacer(Modifier.width(10.dp))
                    Text("Cache pembaruan / berbagi", Modifier.weight(1f))
                    Text(humanSize(snapshot.cache), color = Color.LightGray)
                }
                Button(onClick = { confirm = true }, enabled = snapshot.cache > 0L,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5360FA)),
                    modifier = Modifier.fillMaxWidth().height(55.dp)) {
                    Text("Kosongkan cache ${humanSize(snapshot.cache)}")
                }
                Text(if (cloud.connected)
                    "Dokumen lokal tetap disimpan sebagai salinan kerja. " +
                        "Pengaturan cloud tersedia di Profil → Akun."
                else "Buku dan progres akun tamu disimpan di perangkat. " +
                    "Pembersihan cache tidak menghapus dokumen.",
                    color = Color(0xFFAAAAAA), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false },
        title = { Text("Kosongkan cache?") },
        text = { Text("Hanya unduhan pembaruan dan salinan berbagi sementara yang dihapus. Dokumen tetap aman.") },
        confirmButton = { TextButton(onClick = {
            StorageInspector.clearTemporary(context)
            confirm = false; revision++
        }) { Text("Kosongkan") } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text("Batal") } })
}
