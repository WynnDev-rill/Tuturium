package id.tuturium.app

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Reproduces: a Google Indonesian voice previews successfully but the reader
 * throws an exception when the same voice is used through the media service.
 * Skips only when Google cannot successfully preview on the test device. */
@RunWith(AndroidJUnit4::class)
class ReaderVoiceParityTest {
    @Test fun aVoiceThatPreviewsAlsoStartsReaderPlayback(): Unit = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val initialized = CompletableDeferred<Int>()
        val preview = TextToSpeech(context, { initialized.complete(it) }, GOOGLE_TTS_ENGINE)
        val previous = ReadingSettings.read(context)
        var book: Book? = null
        try {
            assumeTrue("Google TTS unavailable",
                withTimeoutOrNull(15_000) { initialized.await() } == TextToSpeech.SUCCESS)
            val chosen = preview.indonesianCandidates().firstOrNull {
                it.network && it.name != GOOGLE_ID_AUTO
            } ?: preview.indonesianCandidates().firstOrNull {
                it.name != GOOGLE_ID_AUTO
            }
            assumeTrue("Google Indonesian voice unavailable", chosen != null)
            val voice = requireNotNull(chosen)
            assumeTrue("Google Indonesian voice cannot be selected",
                preview.activateIndonesianVoice(voice))
            val previewDone = CompletableDeferred<Boolean>()
            preview.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit
                override fun onDone(utteranceId: String?) { previewDone.complete(true) }
                @Deprecated("Required by Android TTS")
                override fun onError(utteranceId: String?) { previewDone.complete(false) }
                override fun onError(utteranceId: String?, errorCode: Int) {
                    previewDone.complete(false)
                }
            })
            assumeTrue("Google preview request failed",
                preview.speak("Halo, ini uji suara Indonesia.",
                    TextToSpeech.QUEUE_FLUSH, null, "preview-check") == TextToSpeech.SUCCESS)
            assumeTrue("Google Indonesian preview did not complete",
                withTimeoutOrNull(20_000) { previewDone.await() } == true)
            preview.stop()
            preview.shutdown()
            ReadingSettings(voice.name, 1f, 1f).save(context)

            val library = Library.get(context)
            val added = library.add("Reader versus preview verification", "TXT",
                ReadingDocument(listOf(Section("Uji", (
                    "Ini adalah teks bahasa Indonesia yang harus terdengar di pembaca. " +
                    "Pembacaan tidak boleh gagal saat memulai atau berpindah kalimat. "
                ).repeat(90)))))
            book = added
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                scenario.onActivity {
                    assertTrue(ReadingService.command(it, ReadingService.PLAY, added.id, 0))
                }
                val state = withTimeout(25_000) {
                    ReadingService.state.first {
                        it.bookId == added.id && (it.error != null || it.playing)
                    }
                }
                assertNull("Preview succeeded but reader playback failed: " + state.error, state.error)
                delay(1_000)
                assertNull("Reader failed after initially speaking",
                    ReadingService.state.value.error)
            }
        } finally {
            ReadingService.command(context, ReadingService.STOP)
            previous.save(context)
            preview.shutdown()
            book?.let {
                Library.get(context).delete(it.id)
                Library.get(context).deletePermanently(it.id)
            }
        }
    }
}
