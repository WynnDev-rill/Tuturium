package id.tuturium.app

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaybackTest {
    @Test fun localSpeechSurvivesHomeAndAcceptsMediaKeyPauseAndResume() = runBlocking {
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
                withTimeout(30_000) { ReadingService.state.first { it.bookId == book.id && it.playing } }
                shell("input keyevent KEYCODE_HOME")
                delay(1800)
                assertTrue(ReadingService.state.value.playing)
                shell("cmd media_session dispatch pause")
                withTimeout(15_000) { ReadingService.state.first { it.bookId == book.id && !it.playing } }
                shell("cmd media_session dispatch play")
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
