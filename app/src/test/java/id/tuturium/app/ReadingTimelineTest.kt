package id.tuturium.app

import android.media.AudioFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReadingTimelineTest {
    @Test fun sourceProgressWeightsUnevenPassesAndResumesAtTheSavedWord() {
        val document = ReadingDocument(listOf(Section("Bab", "Halo!\nSatu dua tiga empat.")))
        val timeline = ReadingTimeline(document, ListeningOptions())
        assertEquals(0, timeline.position(0, 0).percent)
        assertEquals(20, timeline.position(1, 0).percent)
        assertEquals(40, timeline.position(1, 5).percent)
        assertEquals(3, timeline.position(1, 5).remainingWords)
        assertEquals(100, timeline.position(2, 0).percent)
        assertEquals(0, timeline.position(2, 0).remainingSeconds(400.0, 2f))
    }

    @Test fun skipFiltersAndExtraPausesAffectRemainingAudioButNotSourceProgress() {
        val document = ReadingDocument(listOf(Section("Bab", "Satu (abaikan kata ini).\nDua tiga.")))
        val options = ListeningOptions(skipEnabled = true, paragraphPauseMs = 900)
        val timeline = ReadingTimeline(document, options)
        val beginning = timeline.position(0, 0)
        assertEquals(3, beginning.remainingWords)
        assertEquals(900L, beginning.remainingPauseMs)
        assertEquals(3, beginning.remainingSeconds(500.0, 1f))
        assertEquals(2, beginning.remainingSeconds(500.0, 2f))
        val secondParagraph = timeline.position(1, 0)
        assertEquals(66, secondParagraph.percent)
        assertEquals(0L, secondParagraph.remainingPauseMs)
        assertEquals(2, secondParagraph.remainingWords)
    }

    @Test fun equalSpeedMultipliersRespectDifferentMeasuredPaces() {
        val reading = ReadingPosition(0.5f, 1_200)
        val faster = PaceSample(20, 6_000.0)
        val slower = PaceSample(20, 12_000.0)
        assertEquals(180, reading.remainingSeconds(faster.millisPerWord, 2f))
        assertEquals(360, reading.remainingSeconds(slower.millisPerWord, 2f))
        assertEquals(90, reading.remainingSeconds(faster.millisPerWord, 4f))
        assertEquals(400.0, faster.combinedWith(PaceSample(10, 6_000.0)).millisPerWord, 0.001)
    }

    @Test fun elapsedAndTotalUseSpokenWordsAndExtraPausesAtSameRate() {
        val document = ReadingDocument(listOf(Section("Bab", "Satu (abaikan kata ini).\nDua tiga.")))
        val timeline = ReadingTimeline(document, ListeningOptions(skipEnabled = true, paragraphPauseMs = 900))
        assertEquals("≈ 00:00 / 00:03", playbackTimeCaption(timeline.position(0, 0), 500.0, 1f))
        assertEquals("≈ 00:02 / 00:03", playbackTimeCaption(timeline.position(1, 0), 500.0, 1f))
        assertEquals("≈ 00:03 / 00:03", playbackTimeCaption(timeline.position(document.passes.size, 0), 500.0, 1f))
        assertEquals("≈ 00:00 / 00:02", playbackTimeCaption(timeline.position(0, 0), 500.0, 2f))
    }

    @Test fun playbackTimePreservesLongHourFormat() {
        assertEquals("≈ 03:58 / 16:39:35", playbackTimeCaption(
            ReadingPosition(0.004f, 59737, 0, 59975, 0), 1000.0, 1f))
    }

    @Test fun playbackClockKeepsTotalAndAdvancesWhenCalibrationChanges() {
        val profile = PaceProfile("google:test:2x")
        var measuredPace = 400.0
        val session = NarrationPaceSnapshot { measuredPace }
        val start = ReadingPosition(0.1f, 900, totalWords = 1_000)
        val later = ReadingPosition(0.2f, 800, totalWords = 1_000)

        assertEquals("≈ 00:20 / 03:20", playbackTimeCaption(start, session.millisPerWord(profile), 2f))
        measuredPace = 700.0
        assertEquals("≈ 00:20 / 03:20", playbackTimeCaption(start, session.millisPerWord(profile), 2f))
        assertEquals("≈ 00:40 / 03:20", playbackTimeCaption(later, session.millisPerWord(profile), 2f))
        assertEquals("≈ 00:20 / 01:40", playbackTimeCaption(later, session.millisPerWord(profile), 4f))

        val reopened = NarrationPaceSnapshot { measuredPace }
        assertEquals("≈ 01:10 / 05:50", playbackTimeCaption(later, reopened.millisPerWord(profile), 2f))
    }

    @Test fun timingSpeedDoesNotFollowAOnePassGoogleFallback() {
        val book = Book("id", "Teks", "TXT", 3, 0, 0L)
        val playback = Playback(bookId = book.id, total = 3, effectiveSpeed = 1f, narrationSpeed = 2f)
        assertEquals(2f, playback.estimatedReadingSpeed(book, 2f), 0f)
        assertEquals(2f, playback.copy(effectiveSpeed = 2f).estimatedReadingSpeed(book, 2f), 0f)
        assertEquals(2.05f, playback.copy(narrationSpeed = 2.05f).estimatedReadingSpeed(book, 2f), 0f)
        assertEquals(3f, playback.copy(bookId = "another").estimatedReadingSpeed(book, 3f), 0f)
    }

    @Test fun googlePcmTimingCountsChannelsAndRejectsStaleUtterances() {
        val audio = UtteranceAudioLength()
        audio.reset("current")
        audio.begin("old", 8_000, AudioFormat.ENCODING_PCM_8BIT, 1)
        audio.add("old", 8_000)
        audio.begin("current", 24_000, AudioFormat.ENCODING_PCM_16BIT, 2)
        audio.add("current", 12_000)
        audio.add("current", 36_000)
        assertNull(audio.finish("old"))
        assertEquals(500.0, audio.finish("current")!!, 0.001)
        assertNull(audio.finish("current"))
    }
}
