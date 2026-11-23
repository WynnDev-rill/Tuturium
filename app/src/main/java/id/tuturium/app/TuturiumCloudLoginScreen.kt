package id.tuturium.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Account-first screen: imported documents are retained while awaiting OAuth,
 * but the online library is not exposed until Drive permission and sync succeed. */
@Composable
fun TuturiumCloudLoginScreen(state: TuturiumCloudState, onSignIn: () -> Unit) {
    TuturiumTheme("dark") {
        Box(Modifier.fillMaxSize().background(Color.Black).padding(28.dp),
            contentAlignment = Alignment.Center) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(shape = RoundedCornerShape(28.dp),
                    color = Color(0xFF20202D), modifier = Modifier.size(94.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Cloud, "Google Drive",
                            modifier = Modifier.size(52.dp), tint = Color(0xFF6674FF))
                    }
                }
                Text("Tuturium", style = MaterialTheme.typography.headlineLarge,
                    color = Color.White)
                Text("Buku pribadi, tersedia di perangkatmu melalui Google Drive.",
                    color = Color(0xFFBCBCCC), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onSignIn, enabled = !state.busy,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5063F8))) {
                    if (state.busy) {
                        CircularProgressIndicator(Modifier.size(23.dp),
                            color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Rounded.CloudDone, null)
                        Spacer(Modifier.width(10.dp))
                        Text("Masuk dengan Google")
                    }
                }
                Text(state.message, color = Color(0xFFB9B9C5),
                    textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Lock, null, Modifier.size(16.dp),
                        tint = Color(0xFF9C9CA8))
                    Spacer(Modifier.width(7.dp))
                    Text("Hanya akses data khusus Tuturium di Google Drive.",
                        color = Color(0xFF9C9CA8),
                        style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
