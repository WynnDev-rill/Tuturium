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
        assumeTrue(InstrumentationRegistry.getArguments().getString("tuturiumPlayback") == "true")
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val library = Library.get(instrumentation.targetContext)
        val book = library.add("Uji pemutar", "TXT", ReadingDocument(listOf(
            Section("Uji", ("Musim hujan membawa ketenangan ke desa kecil di tepi hutan " +
                "dengan jalan setapak yang menghubungkan rumah warga ").repeat(5) + ".")
        )))
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                scenario.onActivity { ReadingService.command(it, ReadingService.PLAY, book.id, 0) }
                val started = withTimeout(30_000) {
                    ReadingService.state.first { it.error != null || (it.bookId == book.id && it.playing) }
                }
                assertNull("Android TTS returned an error", started.error)
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
                withTimeout(15_000) { ReadingService.state.first { it.bookId == book.id && !it.playing } }
                controls.play()
                withTimeout(15_000) { ReadingService.state.first { it.bookId == book.id && it.playing } }
            }
        } finally {
            instrumentation.targetContext.stopService(Intent(instrumentation.targetContext, ReadingService::class.java))
            library.delete(book.id)
        }
    }

    private fun shell(command: String) {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        android.os.ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(command)).use { it.readBytes() }
    }
}
