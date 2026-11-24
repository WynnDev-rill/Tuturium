package id.tuturium.app

import android.app.Notification
import android.app.NotificationManager
import android.content.Intent
import android.media.session.MediaController
import android.media.session.MediaSession
import androidx.core.os.BundleCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaybackTest {
    @Test fun localSpeechSurvivesHomeAndRespondsToNotificationTransportControls(): Unit = runBlocking {
        val arguments = InstrumentationRegistry.getArguments()
        val offline = arguments.getString("offlineVoiceSmoke") == "true"
        assumeTrue(offline || arguments.getString("tuturiumPlayback") == "true")
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val original = ReadingSettings.read(context)
        val pack = if (offline) OfflineCatalog.model("supertonic3") else null
        if (pack != null) OfflineModelStore.install(context, pack) {}
        val library = Library.get(context)
        val book = library.add("Uji pemutar", "TXT", ReadingDocument(listOf(
            Section("Uji", "Musim hujan membawa ketenangan ke desa kecil di tepi hutan. " +
                "Jalan setapak menghubungkan rumah semua warga di desa itu. " +
                "Hari ini banyak anak bermain di lapangan di tengah desa.")
        )))
        try {
            if (pack != null) ReadingSettings(pack.voices.first().id, 1f, 1f).save(context)
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                scenario.onActivity { ReadingService.command(it, ReadingService.PLAY, book.id, 0) }
                val started = withTimeout(60_000) {
                    ReadingService.state.first { it.bookId == book.id && (it.error != null || it.playing) }
                }
                assertNull("TTS returned an error", started.error)
                val notificationManager = instrumentation.targetContext.getSystemService(NotificationManager::class.java)
                val token = withTimeout(10_000) {
                    var found: MediaSession.Token? = null
                    while (found == null) {
                        found = notificationManager.activeNotifications.firstNotNullOfOrNull {
                            BundleCompat.getParcelable(it.notification.extras, Notification.EXTRA_MEDIA_SESSION, MediaSession.Token::class.java)
                        }
                        if (found == null) delay(100)
                    }
                    checkNotNull(found)
                }
                val controls = MediaController(instrumentation.targetContext, token).transportControls
                shell("input keyevent KEYCODE_HOME")
                delay(1800)
                assertTrue("Playback stopped when the activity left the foreground", ReadingService.state.value.playing)
                controls.pause()
                withTimeout(30_000) { ReadingService.state.first { it.bookId == book.id && !it.playing && !it.loading } }
                controls.play()
                withTimeout(30_000) { ReadingService.state.first { it.bookId == book.id && it.playing } }
            }
        } finally {
            context.stopService(Intent(context, ReadingService::class.java))
            if (pack != null) original.save(context)
            library.delete(book.id)
            library.deletePermanently(book.id)
        }
    }

    private fun shell(command: String) {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        android.os.ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(command)).use { it.readBytes() }
    }
}
