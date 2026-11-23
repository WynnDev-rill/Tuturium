package id.tuturium.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import coil.compose.SubcomposeAsyncImage

private val accountSurface = Color(0xFF252525)
private val accountMuted = Color(0xFFAAAAAA)
private val accountAccent = Color(0xFF5362F5)

@Composable
internal fun GoogleAvatar(photoUrl: String?, size: Dp) {
    Box(Modifier.size(size).clip(CircleShape).background(accountSurface),
        contentAlignment = Alignment.Center) {
        @Composable
        fun Placeholder() {
            Icon(Icons.Rounded.Person, null, Modifier.size(size * 0.61f),
                tint = Color.White)
        }
        if (!photoUrl.isNullOrBlank() && photoUrl.startsWith("https://")) {
            SubcomposeAsyncImage(
                model = photoUrl, contentDescription = "Foto profil Google",
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
                loading = { Placeholder() }, error = { Placeholder() }
            )
        } else Placeholder()
    }
}

/** Simple account details like a mobile profile sheet; only this page handles
 * Google sign-in, Drive synchronization and logout. Guest access remains local. */
@Composable
internal fun TuturiumAccountScreen(
    onBack: () -> Unit, onSignIn: () -> Unit,
    onSync: () -> Unit, onSignOut: () -> Unit
) {
    val cloud by TuturiumCloud.state.collectAsStateWithLifecycle()
    val name = if (cloud.connected) {
        cloud.displayName?.takeIf { it.isNotBlank() }
            ?: cloud.email?.substringBefore('@')
            ?: "Akun Google"
    } else "Akun tamu"

    Column(Modifier.fillMaxSize().background(Color.Black)
        .padding(horizontal = 20.dp)) {
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
            GoogleAvatar(if (cloud.connected) cloud.photoUrl else null, 96.dp)
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
                AccountDetailRow("Penyimpanan Google Drive",
                    if (cloud.connected) "Aktif" else "Belum terhubung")
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = if (cloud.connected) onSync else onSignIn,
            enabled = !cloud.busy,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accountAccent)) {
            if (cloud.busy) {
                CircularProgressIndicator(Modifier.size(20.dp),
                    color = Color.White, strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
            }
            Text(if (cloud.connected) "Sinkronkan Google Drive" else "Masuk dengan Google",
                color = Color.White)
        }
        if (cloud.connected) {
            TextButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                Text("Keluar akun", color = accountMuted)
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(if (!cloud.connected &&
                cloud.message == TuturiumCloudState().message)
                "Masuk bersifat opsional. Buku akun tamu tetap disimpan di perangkat."
            else cloud.message,
            color = accountMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun AccountDetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().heightIn(min = 69.dp)
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
