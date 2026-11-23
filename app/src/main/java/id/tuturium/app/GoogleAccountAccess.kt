package id.tuturium.app

import android.accounts.Account
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import java.io.IOException

internal const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"

internal fun driveAuthorization(email: String): AuthorizationRequest =
    AuthorizationRequest.builder()
        .setAccount(Account(email, "com.google"))
        .setRequestedScopes(listOf(Scope(DRIVE_APPDATA_SCOPE)))
        .setOptOutIncludingGrantedScopes(true)
        .build()

internal fun isAccountReauthFailure(message: String?): Boolean =
    message?.contains("account reauth failed", ignoreCase = true) == true

internal fun googleAccessMessage(error: Exception, drive: Boolean): String {
    if (isAccountReauthFailure(error.message)) {
        return "Google perlu memverifikasi ulang akun. Buka setelan akun Android, selesaikan verifikasi Google, lalu coba lagi."
    }
    return when (error) {
        is GetCredentialCancellationException -> "Pemilihan akun Google dibatalkan."
        is NoCredentialException -> "Pilih atau tambahkan akun Google melalui setelan Android, lalu coba lagi."
        is GetCredentialProviderConfigurationException -> "Layanan login Google belum tersedia. Perbarui Google Play Services."
        is IOException -> "Koneksi ke Google gagal. Coba lagi saat online."
        is ApiException -> when (error.statusCode) {
            7 -> "Koneksi ke Google gagal. Coba lagi saat online."
            10 -> "Google menolak konfigurasi aplikasi. Periksa package, SHA-1 APK dan klien OAuth di Google Cloud."
            12501, 16 -> "Permintaan Google dibatalkan. Coba lagi untuk melanjutkan."
            else -> if (drive) "Izin Drive gagal (kode ${error.statusCode}). Periksa akses akun ini di Google Cloud."
                else "Masuk dengan Google gagal (kode ${error.statusCode})."
        }
        else -> if (drive) "Izin Google Drive gagal. Periksa akses akun dan coba lagi."
            else "Masuk dengan Google gagal. Periksa akun, koneksi, dan Google Play Services."
    }
}
