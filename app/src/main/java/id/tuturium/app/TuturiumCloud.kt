package id.tuturium.app

import android.content.Context
import java.io.File
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Use the same account for the local migration cache and Google Drive.
 * Switching accounts without an isolated cache would leak books to another user. */
fun isCompatibleCloudAccount(saved: String?, next: String): Boolean =
    next.isNotBlank() && (saved == null ||
        saved.trim().lowercase(Locale.ROOT) == next.trim().lowercase(Locale.ROOT))

data class TuturiumCloudState(
    val email: String? = null,
    val connected: Boolean = false,
    val busy: Boolean = false,
    val message: String = "Masuk dengan Google untuk menggunakan penyimpanan Drive.",
    val lastSynced: Long = 0L
)

/** One-account cloud synchronizer. Remote appData is authoritative after sign-in.
 * Until migration and round-trip verification succeed, the legacy local library
 * is retained to prevent permanent loss of pre-cloud books. */
internal object TuturiumCloud {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private val mutable = MutableStateFlow(TuturiumCloudState())
    val state = mutable.asStateFlow()

    @Volatile private var token: String? = null
    @Volatile private var email: String? = null
    private var pending: Job? = null
    private var dirtySince: Long = 0L

    fun message(text: String) { mutable.value = mutable.value.copy(message = text, busy = false) }

    suspend fun connect(context: Context, account: String, accessToken: String) =
        withContext(Dispatchers.IO) {
            val saved = context.getSharedPreferences("tuturium_cloud", Context.MODE_PRIVATE)
                .getString("account_email", null)
            require(isCompatibleCloudAccount(saved, account)) {
                "Perangkat ini menyimpan pustaka dari akun Google berbeda. " +
                    "Cadangkan atau pindahkan data lama sebelum mengganti akun."
            }
            require(accessToken.isNotBlank()) { "Google Drive belum memberikan izin." }
            mutex.withLock {
                mutable.value = mutable.value.copy(busy = true,
                    message = "Menghubungkan Google Drive…")
                val api = DriveAppDataClient(accessToken)
                val remote = api.findArchive()
                if (remote != null) {
                    val file = File.createTempFile("tuturium-cloud-restore-", ".zip", context.cacheDir)
                    try {
                        mutable.value = mutable.value.copy(message = "Memulihkan pustaka dari Drive…")
                        api.download(remote, file)
                        DriveArchive(context).restore(file)
                    } finally { file.delete() }
                }
                // First migration or merging newer local records. The Google Drive
                // archive is verified before this account becomes active.
                mutable.value = mutable.value.copy(message = "Mengunggah dan memverifikasi pustaka…")
                val snapshot = File.createTempFile("tuturium-cloud-upload-", ".zip", context.cacheDir)
                try {
                    DriveArchive(context).export(snapshot)
                    val uploaded = api.upload(snapshot, remote)
                    require(uploaded.md5 != null &&
                        uploaded.md5 == DriveAppDataClient.checksum(snapshot)) {
                        "Google Drive belum memverifikasi checksum arsip."
                    }
                } finally { snapshot.delete() }
                check(context.getSharedPreferences("tuturium_cloud", Context.MODE_PRIVATE).edit()
                    .putString("account_email", account).commit()) {
                    "Akun Google tidak dapat disimpan."
                }
                token = accessToken
                email = account
                mutable.value = TuturiumCloudState(
                    email = account, connected = true, busy = false,
                    message = "Tersinkronisasi ke Google Drive",
                    lastSynced = System.currentTimeMillis()
                )
            }
        }

    fun markDirty(context: Context) {
        if (token.isNullOrBlank() || email == null) return
        val now = System.currentTimeMillis()
        if (dirtySince == 0L) dirtySince = now
        val delayMs = if (now - dirtySince > 120_000L) 0L else 12_000L
        pending?.cancel()
        pending = scope.launch {
            delay(delayMs)
            uploadNow(context.applicationContext)
        }
    }

    fun uploadSoon(context: Context) {
        if (token.isNullOrBlank()) return
        pending?.cancel()
        pending = scope.launch { uploadNow(context.applicationContext) }
    }

    suspend fun syncNow(context: Context) {
        pending?.cancel()
        withContext(Dispatchers.IO) { uploadNow(context.applicationContext) }
    }

    private suspend fun uploadNow(context: Context) {
        mutex.withLock {
            val currentToken = token ?: return
            if (email == null) return
            mutable.value = mutable.value.copy(busy = true, message = "Menyimpan ke Google Drive…")
            val snapshot = File.createTempFile("tuturium-cloud-sync-", ".zip", context.cacheDir)
            try {
                DriveArchive(context).export(snapshot)
                val api = DriveAppDataClient(currentToken)
                val remote = api.findArchive()
                val uploaded = api.upload(snapshot, remote)
                require(uploaded.md5 != null &&
                    uploaded.md5 == DriveAppDataClient.checksum(snapshot)) {
                    "Google Drive belum memverifikasi checksum unggahan."
                }
                dirtySince = 0L
                mutable.value = mutable.value.copy(busy = false,
                    message = "Tersinkronisasi ke Google Drive",
                    lastSynced = System.currentTimeMillis())
            } catch (error: Exception) {
                android.util.Log.e("TuturiumCloud", "Drive sync failed", error)
                mutable.value = mutable.value.copy(busy = false,
                    message = error.message ?: "Sinkronisasi Drive gagal. Coba lagi.")
            } finally {
                snapshot.delete()
            }
        }
    }

    /** Instrumentation-only login bypass for emulator UI regression tests.
     * Never grants a Drive token and cannot run in release builds. */
    fun debugEnableForTests() {
        if (BuildConfig.DEBUG) mutable.value = TuturiumCloudState(
            email = "instrumentation@localhost", connected = true,
            message = "Mode uji antarmuka tanpa akun Google")
    }

    fun debugResetForTests() {
        if (BuildConfig.DEBUG) mutable.value = TuturiumCloudState()
    }

    /** Logout does not delete local pre-migration documents. */
    fun disconnect() {
        pending?.cancel()
        token = null
        email = null
        dirtySince = 0L
        mutable.value = TuturiumCloudState()
    }

    /** On Android activity recreation, renew authorization without writing
     * access tokens to SharedPreferences or to the Drive archive. */
    suspend fun reconnectToken(context: Context, account: String, accessToken: String) =
        withContext(Dispatchers.IO) {
            if (state.value.connected && state.value.email == account) {
                token = accessToken
                uploadSoon(context)
            } else {
                connect(context, account, accessToken)
            }
        }
}
