package id.tuturium.app

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ListeningStatsTest {
    @Test fun weeklySummaryIncludesOnlyRequestedWeek() {
        val today = LocalDate.of(2026, 11, 23)
        val data = listOf(
            ListeningDay(today, durationMs = 120_000L, words = 100, savedMs = 60_000L,
                speedSum = 1500L, samples = 1L),
            ListeningDay(today.minusDays(7), durationMs = 500_000L, words = 500)
        )
        val summary = ListeningStats.summary(data, "Minggu", today)
        assertEquals(120_000L, summary.durationMs)
        assertEquals(100L, summary.words)
        assertEquals(60_000L, summary.savedMs)
        assertEquals(1.5f, summary.averageSpeed, 0.001f)
        assertEquals(7, summary.buckets.size)
    }

    @Test fun yearlySummaryHasTwelveBuckets() {
        val today = LocalDate.of(2026, 11, 23)
        val summary = ListeningStats.summary(listOf(
            ListeningDay(LocalDate.of(2026, 4, 1), words = 12)
        ), "Tahun", today)
        assertEquals(12, summary.buckets.size)
        assertEquals(12L, summary.buckets[3].words)
        assertEquals(12L, summary.words)
    }
}
