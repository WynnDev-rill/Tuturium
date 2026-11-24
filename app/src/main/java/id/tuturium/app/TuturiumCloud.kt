package id.tuturium.app

import android.content.Context
import android.os.SystemClock
import com.google.android.gms.auth.api.identity.ClearTokenRequest
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.tasks.await
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
    val lastSynced: Long = 0L,
    val failed: Boolean = false
) {
    val driveStatus: String get() = when {
        email == null -> "Belum terhubung"
        busy && connected -> "Menyinkronkan…"
        busy -> "Menghubungkan…"
        failed -> "Gagal"
        connected -> "Terhubung"
        else -> "Menunggu"
    }
}

internal object TuturiumCloud {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private val mutable = MutableStateFlow(TuturiumCloudState())
    val state = mutable.asStateFlow()

    @Volatile private var token: String? = null
    @Volatile private var authorizedAt = 0L
    private var pending: Job? = null
    private var firstDirtyAt = 0L
    private var lastDirtyAt = 0L
    @Volatile private var retryAfter = 0L
    @Volatile private var requiresConsent = false

    val shouldReconnect: Boolean get() = state.value.email != null && !state.value.busy &&
        (!state.value.connected || tokenExpired())

    private fun tokenExpired() = token == null || SystemClock.elapsedRealtime() - authorizedAt >= 45 * 60_000L

    fun hasAttemptedDriveConsent(context: Context) = prefs(context).getBoolean("drive_consent_attempted", false)
    fun rememberDriveConsentAttempt(context: Context) {
        prefs(context).edit().putBoolean("drive_consent_attempted", true).apply()
    }

    fun message(text: String) { mutable.update { it.copy(message = text, busy = false) } }
    fun waiting(text: String) { mutable.update { it.copy(message = text, busy = true, failed = false) } }

    fun restoreIdentity(context: Context) {
        if (state.value.email != null) return
        val saved = prefs(context)
        val account = saved.getString("session_email", null) ?: return
        mutable.value = TuturiumCloudState(account, saved.getString("display_name", null),
            saved.getString("photo_url", null), message = "Google Drive akan diperiksa otomatis.",
            lastSynced = saved.getLong("last_synced", 0L))
    }

    fun signIn(context: Context, email: String, displayName: String?, photoUrl: String?) {
        val account = email.trim()
        if (!isCompatibleCloudAccount(prefs(context).getString("account_email", null), account)) {
            throw CloudAccessException("Pustaka ini terhubung ke akun Google lain. Pilih akun yang sebelumnya digunakan.")
        }
        val previous = state.value.takeIf { isMatchingDriveAccount(account, it.email) }
        val name = displayName?.trim()?.take(120)?.takeIf { it.isNotEmpty() } ?: previous?.displayName
        val photo = photoUrl?.takeIf { it.startsWith("https://") }?.take(2048) ?: previous?.photoUrl
        prefs(context).edit().putString("session_email", account)
            .putString("display_name", name).putString("photo_url", photo).apply()
        mutable.value = TuturiumCloudState(account, name, photo,
            message = "Memeriksa Google Drive…", lastSynced = previous?.lastSynced ?: 0L)
    }

    suspend fun connect(context: Context, account: String, accessToken: String) =
        withContext(Dispatchers.IO) {
            cancelPending()
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
                syncWithRenewal(context, account, accessToken)
                requiresConsent = false
                retryAfter = 0L
                rememberDriveConsentAttempt(context)
                mutable.update { it.copy(connected = true, failed = false) }
            }
        }

    @Synchronized
    fun markDirty(context: Context) {
        val account = state.value
        if (account.email == null || (token == null && account.lastSynced == 0L) || requiresConsent ||
            SystemClock.elapsedRealtime() < retryAfter
        ) return
        lastDirtyAt = SystemClock.elapsedRealtime()
        if (pending?.isActive == true) return
        firstDirtyAt = lastDirtyAt
        pending = scope.launch {
            var waitMs: Long
            do {
                waitMs = synchronized(this@TuturiumCloud) {
                    val due = minOf(firstDirtyAt + 120_000L, lastDirtyAt + 12_000L)
                    (due - android.os.SystemClock.elapsedRealtime()).also {
                        if (it <= 0L) pending = null
                    }
                }
                if (waitMs > 0) delay(waitMs)
            } while (waitMs > 0)
            mutex.withLock {
                val email = state.value.email ?: return@withLock
                try {
                    val currentToken = if (tokenExpired()) renewToken(context, email) else checkNotNull(token)
                    syncWithRenewal(context, email, currentToken)
                    retryAfter = 0L
                } catch (error: CancellationException) {
                    throw error
                } catch (error: DriveConsentRequired) {
                    requiresConsent = true
                    driveFailure(error.message.orEmpty())
                } catch (error: DriveHttpException) {
                    if (error.status == 401 || error.status == 403) token = null
                    retryAfter = SystemClock.elapsedRealtime() + 60_000L
                    driveFailure(error.message.orEmpty())
                } catch (error: Exception) {
                    retryAfter = SystemClock.elapsedRealtime() + 60_000L
                    driveFailure(googleAccessMessage(error, drive = true))
                } finally {
                    mutable.update { it.copy(busy = false) }
                }
            }
        }
    }

    private suspend fun syncWithRenewal(context: Context, email: String, accessToken: String) {
        try {
            sync(context.applicationContext, DriveAppDataClient(accessToken))
            if (token != accessToken) {
                token = accessToken
                authorizedAt = SystemClock.elapsedRealtime()
            }
        } catch (error: DriveHttpException) {
            if (error.status != 401) throw error
            token = null
            val client = Identity.getAuthorizationClient(context.applicationContext)
            client.clearToken(ClearTokenRequest.builder().setToken(accessToken).build()).await()
            sync(context.applicationContext, DriveAppDataClient(renewToken(context, email)))
        }
    }

    private class DriveConsentRequired : CloudAccessException(
        "Google Drive memerlukan izin. Buka status Drive untuk melanjutkan.")

    private suspend fun renewToken(context: Context, email: String): String {
        val client = Identity.getAuthorizationClient(context.applicationContext)
        val granted = client.authorize(driveAuthorization(email)).await()
        val accessToken = granted.accessToken
        if (granted.hasResolution() || accessToken.isNullOrBlank() ||
            DRIVE_APPDATA_SCOPE !in granted.grantedScopes
        ) throw DriveConsentRequired()
        if (!isMatchingDriveAccount(email, DriveAppDataClient(accessToken).accountEmail()))
            throw CloudAccessException("Izin Drive diberikan untuk akun berbeda.")
        token = accessToken
        authorizedAt = SystemClock.elapsedRealtime()
        return accessToken
    }

    private fun sync(context: Context, api: DriveAppDataClient) {
        val remote = api.findArchive()
        val saved = prefs(context)
        if (remote != null && (remote.md5 == null || saved.getString("last_remote_md5", null) != remote.md5)) {
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
            val syncedAt = System.currentTimeMillis()
            saved.edit().putString("last_remote_md5", digest).putLong("last_synced", syncedAt).apply()
            mutable.update { it.copy(busy = false, connected = true, failed = false,
                message = "Tersinkronisasi ke Google Drive", lastSynced = syncedAt) }
        } finally {
            snapshot.delete()
        }
    }

    fun driveFailure(text: String) {
        mutable.update { it.copy(connected = false, busy = false, failed = true, message = text) }
    }

    @Synchronized
    private fun cancelPending() {
        pending?.cancel()
        pending = null
    }

    suspend fun disconnect(context: Context) {
        cancelPending()
        mutex.withLock {
            token = null
            authorizedAt = 0L
            retryAfter = 0L
            requiresConsent = false
            prefs(context).edit().remove("session_email").remove("display_name").remove("photo_url").apply()
            mutable.value = TuturiumCloudState()
        }
    }

    private fun prefs(context: Context) = context.getSharedPreferences("tuturium_cloud", Context.MODE_PRIVATE)
}
