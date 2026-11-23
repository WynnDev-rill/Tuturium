package id.tuturium.app

import android.content.Context
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** A local library is linked to one Drive account after its first cloud transfer. */
fun isCompatibleCloudAccount(saved: String?, next: String): Boolean =
    next.isNotBlank() && (saved == null || saved.trim().equals(next.trim(), ignoreCase = true))

internal fun isMatchingDriveAccount(selected: String, authorized: String?): Boolean =
    selected.isNotBlank() && !authorized.isNullOrBlank() &&
        selected.trim().equals(authorized.trim(), ignoreCase = true)

internal open class CloudAccessException(message: String) : java.io.IOException(message)
internal class DriveHttpException(val status: Int, message: String) : CloudAccessException(message)

data class TuturiumCloudState(
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val connected: Boolean = false,
    val busy: Boolean = false,
    val message: String = "Masuk dengan Google untuk menggunakan penyimpanan Drive.",
    val lastSynced: Long = 0L
)

internal object TuturiumCloud {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private val mutable = MutableStateFlow(TuturiumCloudState())
    val state = mutable.asStateFlow()

    @Volatile private var token: String? = null
    @Volatile private var authorizedAt = 0L
    private var pending: Job? = null
    private var lastRemoteChecksum: String? = null

    fun message(text: String) { mutable.update { it.copy(message = text, busy = false) } }
    fun waiting(text: String) { mutable.update { it.copy(message = text, busy = true) } }

    fun restoreIdentity(context: Context) {
        if (state.value.email != null) return
        val saved = prefs(context)
        val account = saved.getString("session_email", null) ?: return
        mutable.value = TuturiumCloudState(account, saved.getString("display_name", null),
            saved.getString("photo_url", null), message = "Hubungkan Google Drive untuk melanjutkan sinkronisasi.")
    }

    fun signIn(context: Context, email: String, displayName: String?, photoUrl: String?) {
        val account = email.trim()
        if (!isCompatibleCloudAccount(prefs(context).getString("account_email", null), account)) {
            throw CloudAccessException("Pustaka ini terhubung ke akun Google lain. Pilih akun yang sebelumnya digunakan.")
        }
        val name = displayName?.trim()?.take(120)
        val photo = photoUrl?.takeIf { it.startsWith("https://") }?.take(2048)
        prefs(context).edit().putString("session_email", account)
            .putString("display_name", name).putString("photo_url", photo).apply()
        mutable.value = TuturiumCloudState(account, name, photo,
            message = "Akun Google siap. Hubungkan Drive untuk mengaktifkan sinkronisasi.")
    }

    suspend fun connect(context: Context, account: String, accessToken: String) =
        withContext(Dispatchers.IO) {
            pending?.cancel()
            mutex.withLock {
                waiting("Memverifikasi akun Google Drive…")
                val api = DriveAppDataClient(accessToken)
                if (!isMatchingDriveAccount(account, api.accountEmail())) {
                    throw CloudAccessException("Izin Drive diberikan untuk akun berbeda. Pilih akun Google yang sama.")
                }
                val saved = prefs(context)
                if (!isCompatibleCloudAccount(saved.getString("account_email", null), account)) {
                    throw CloudAccessException("Pustaka ini terhubung ke akun Google lain.")
                }
                // Remember the account before applying a remote snapshot or sending local data.
                if (!saved.edit().putString("account_email", account).commit()) {
                    throw CloudAccessException("Akun Drive tidak dapat disimpan di perangkat.")
                }
                sync(context, api)
                token = accessToken
                authorizedAt = android.os.SystemClock.elapsedRealtime()
                mutable.update { it.copy(connected = true) }
            }
        }

    @Synchronized
    fun markDirty(context: Context) {
        if (token == null || !state.value.connected) return
        pending?.cancel()
        pending = scope.launch {
            delay(12_000)
            mutex.withLock {
                val currentToken = token ?: return@withLock
                if (android.os.SystemClock.elapsedRealtime() - authorizedAt >= 45 * 60_000L) {
                    needsAuthorization("Sesi Drive berakhir. Hubungkan kembali Drive untuk menyinkronkan perubahan.")
                    return@withLock
                }
                try {
                    sync(context.applicationContext, DriveAppDataClient(currentToken))
                } catch (error: CancellationException) {
                    throw error
                } catch (error: DriveHttpException) {
                    if (error.status == 401 || error.status == 403) needsAuthorization(error.message.orEmpty())
                    else message(error.message.orEmpty())
                } catch (_: Exception) {
                    message("Sinkronisasi Drive gagal. Pustaka lokal tetap tersedia.")
                } finally {
                    mutable.update { it.copy(busy = false) }
                }
            }
        }
    }

    private fun sync(context: Context, api: DriveAppDataClient) {
        val remote = api.findArchive()
        if (remote != null && (lastRemoteChecksum == null || lastRemoteChecksum != remote.md5)) {
            val file = File.createTempFile("tuturium-cloud-restore-", ".zip", context.cacheDir)
            try {
                waiting("Menyatukan pustaka dari Google Drive…")
                api.download(remote, file)
                DriveArchive(context).restore(file)
            } finally {
                file.delete()
            }
        }
        val snapshot = File.createTempFile("tuturium-cloud-upload-", ".zip", context.cacheDir)
        try {
            waiting("Menyimpan pustaka ke Google Drive…")
            DriveArchive(context).export(snapshot)
            val digest = DriveAppDataClient.checksum(snapshot)
            if (remote == null || remote.md5 != digest) {
                val uploaded = api.upload(snapshot, remote)
                if (uploaded.md5 != digest) throw CloudAccessException("Google Drive belum memverifikasi arsip.")
            }
            lastRemoteChecksum = digest
            mutable.update { it.copy(busy = false, message = "Tersinkronisasi ke Google Drive",
                lastSynced = System.currentTimeMillis()) }
        } finally {
            snapshot.delete()
        }
    }

    fun needsAuthorization(text: String) {
        token = null
        authorizedAt = 0L
        mutable.update { it.copy(connected = false, busy = false, message = text) }
    }

    suspend fun disconnect(context: Context) {
        pending?.cancel()
        mutex.withLock {
            token = null
            authorizedAt = 0L
            lastRemoteChecksum = null
            prefs(context).edit().remove("session_email").remove("display_name").remove("photo_url").apply()
            mutable.value = TuturiumCloudState()
        }
    }

    private fun prefs(context: Context) = context.getSharedPreferences("tuturium_cloud", Context.MODE_PRIVATE)
}
