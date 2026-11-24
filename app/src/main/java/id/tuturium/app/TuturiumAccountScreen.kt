package id.tuturium.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.disk.DiskCache

private val accountSurface = Color(0xFF252525)
private val accountMuted = Color(0xFFAAAAAA)
private val accountAccent = Color(0xFF5362F5)

private var cachedAvatarLoader: ImageLoader? = null

@Composable
internal fun GoogleAvatar(photoUrl: String?, size: Dp, description: String = "Foto profil Google") {
    val context = LocalContext.current.applicationContext
    val loader = remember(context) {
        cachedAvatarLoader ?: ImageLoader.Builder(context)
            .diskCache {
                DiskCache.Builder().directory(context.cacheDir.resolve("google-avatars"))
                    .maxSizeBytes(4 * 1024 * 1024L).build()
            }
            .respectCacheHeaders(false)
            .build().also { cachedAvatarLoader = it }
    }
    Box(Modifier.size(size).clip(CircleShape).background(accountSurface)
        .semantics { contentDescription = description },
        contentAlignment = Alignment.Center) {
        @Composable
        fun Placeholder() {
            Icon(Icons.Rounded.Person, null, Modifier.size(size * 0.61f),
                tint = Color.White)
        }
        Placeholder()
        if (photoUrl?.startsWith("https://") == true) {
            key(photoUrl) {
                AsyncImage(
                    model = photoUrl, imageLoader = loader, contentDescription = null,
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                )
            }
        }
    }
}

/** Simple account details like a mobile profile sheet; only this page handles
 * Google sign-in, Drive synchronization and logout. Guest access remains local. */
@Composable
internal fun TuturiumAccountScreen(
    onBack: () -> Unit, onSignIn: () -> Unit,
    onRetryDrive: () -> Unit, onSignOut: () -> Unit
) {
    val cloud by TuturiumCloud.state.collectAsStateWithLifecycle()
    var showDrive by remember { mutableStateOf(false) }
    val name = if (cloud.email != null) {
        cloud.displayName?.takeIf { it.isNotBlank() }
            ?: cloud.email?.substringBefore('@')
            ?: "Akun Google"
    } else "Akun tamu"

    Column(Modifier.fillMaxSize().background(Color.Black)
        .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 28.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
            }
            Spacer(Modifier.weight(1f))
            Text("Akun", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(48.dp))
        }
        Column(Modifier.fillMaxWidth().padding(bottom = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            GoogleAvatar(cloud.photoUrl, 96.dp)
            Spacer(Modifier.height(14.dp))
            Text(name, style = MaterialTheme.typography.titleLarge, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text(if (cloud.connected) "Google Drive terhubung" else "Penyimpanan lokal",
                style = MaterialTheme.typography.bodySmall, color = accountMuted)
        }
        Surface(Modifier.fillMaxWidth(), color = accountSurface,
            shape = RoundedCornerShape(22.dp)) {
            Column {
                AccountDetailRow("Email", cloud.email ?: "Belum masuk")
                HorizontalDivider(color = Color.White.copy(alpha = 0.09f))
                AccountDetailRow("Penyimpanan Google Drive", cloud.driveStatus,
                    modifier = Modifier.clickable { showDrive = true })
            }
        }
        Spacer(Modifier.height(24.dp))
        if (cloud.email == null) {
            Button(onClick = onSignIn, enabled = !cloud.busy,
                modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accountAccent)) {
                if (cloud.busy) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                }
                Text("Masuk dengan Google", color = Color.White)
            }
        }
        if (cloud.email != null) {
            TextButton(onClick = onSignOut, enabled = !cloud.busy, modifier = Modifier.fillMaxWidth()) {
                Text("Keluar akun", color = accountMuted)
            }
        }
        if (cloud.email == null) {
            Spacer(Modifier.height(14.dp))
            Text(if (cloud.message == TuturiumCloudState().message)
                "Masuk bersifat opsional. Buku akun tamu tetap disimpan di perangkat." else cloud.message,
                color = accountMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall)
        }
    }
    if (showDrive) TuturiumDialog(onDismissRequest = { showDrive = false },
        title = { Text("Penyimpanan Google Drive") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(cloud.driveStatus, style = MaterialTheme.typography.titleMedium)
                Text(cloud.message)
                if (cloud.lastSynced > 0) {
                    val date = java.text.DateFormat.getDateTimeInstance().format(java.util.Date(cloud.lastSynced))
                    Text("Sinkronisasi terakhir: $date", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { showDrive = false }) { Text("Tutup") }
        },
        dismissButton = if (cloud.email != null && cloud.failed && !cloud.busy) {
            { TextButton(onClick = onRetryDrive) { Text("Coba lagi") } }
        } else null)

}

@Composable
private fun AccountDetailRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().heightIn(min = 69.dp)
        .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium, color = Color.White)
        Spacer(Modifier.width(12.dp))
        Text(value, color = accountMuted, maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall)
    }
}
