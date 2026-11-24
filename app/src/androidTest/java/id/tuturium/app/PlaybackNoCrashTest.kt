package id.tuturium.app

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaybackNoCrashTest {
    @Test fun filtersCompileAndUseLiteralClosingBracketsOnAndroidIcu() {
        val source = "Baca [sela] kata {rumus} lagi."
        val filtered = prepareSpeech(source, ListeningOptions(skipEnabled = true))
        assertEquals("Baca  kata  lagi.", filtered.text)
        assertEquals(source.indexOf("lagi"), filtered.offsets[filtered.text.indexOf("lagi")])
    }

    @Test fun playOnFreshImportNeverCrashesEvenIfGoogleVoiceDataIsUnavailable(): Unit = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val library = Library.get(context)
        val book = library.add("First play regression", "TXT",
            ReadingDocument(listOf(Section("Tes",
                ("Ini adalah contoh kalimat bahasa Indonesia untuk memeriksa suara. ").repeat(70)))))
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                scenario.onActivity {
                    assertEquals(true, ReadingService.command(it, ReadingService.PLAY, book.id, 0))
                }
                val result = withTimeout(32_000) {
                    ReadingService.state.first {
                        it.bookId == book.id && (it.playing || it.error != null)
                    }
                }
                assertNotNull(result.bookId)
                scenario.onActivity { activity ->
                    assertEquals(false, activity.isFinishing)
                }
            }
        } finally {
            ReadingService.command(context, ReadingService.STOP)
            library.delete(book.id)
            library.deletePermanently(book.id)
        }
    }
}
