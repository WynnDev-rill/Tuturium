package id.tuturium.app

import android.content.Context
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import java.util.Locale

data class ListeningDay(
    val date: LocalDate, val durationMs: Long = 0L, val words: Long = 0L,
    val savedMs: Long = 0L, val speedSum: Long = 0L, val samples: Long = 0L
)
data class StatsBucket(val durationMs: Long = 0, val words: Long = 0)
data class StatsSummary(
    val durationMs: Long, val words: Long, val savedMs: Long,
    val averageSpeed: Float, val buckets: List<StatsBucket>, val labels: List<String>
)

/** Local-only listening metrics. Time is committed when playback pauses or stops. */
object ListeningStats {
    private const val FILE = "listening_stats"
    private fun today() = LocalDate.now().toString()
    private fun prefs(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun recordDuration(context: Context, durationMs: Long, speed: Float) {
        if (durationMs <= 0L) return
        val p = prefs(context)
        val day = today()
        p.edit()
            .putLong("$day.duration", p.getLong("$day.duration", 0L) + durationMs)
            .putLong("$day.saved", p.getLong("$day.saved", 0L) +
                (durationMs * (speed - 1f).coerceAtLeast(0f)).toLong())
            .apply()
    }

    fun recordUtterance(context: Context, text: String, speed: Float) {
        val count = text.split(Regex("\\s+")).count { it.any(Char::isLetterOrDigit) }
        if (count == 0) return
        val p = prefs(context)
        val day = today()
        p.edit()
            .putLong("$day.words", p.getLong("$day.words", 0L) + count)
            .putLong("$day.speedSum", p.getLong("$day.speedSum", 0L) + (speed * 1000).toLong())
            .putLong("$day.samples", p.getLong("$day.samples", 0L) + 1)
            .apply()
    }

    fun read(context: Context): List<ListeningDay> {
        val all = prefs(context)
        val dates = all.all.keys.mapNotNull { key ->
            runCatching { LocalDate.parse(key.substringBefore('.')) }.getOrNull()
        }.distinct()
        return dates.map { date ->
            val key = date.toString()
            ListeningDay(
                date = date, durationMs = all.getLong("$key.duration", 0L),
                words = all.getLong("$key.words", 0L), savedMs = all.getLong("$key.saved", 0L),
                speedSum = all.getLong("$key.speedSum", 0L),
                samples = all.getLong("$key.samples", 0L)
            )
        }
    }

    fun summary(history: List<ListeningDay>, period: String, today: LocalDate = LocalDate.now()): StatsSummary {
        val starts: List<LocalDate>
        val labels: List<String>
        val end: LocalDate
        when (period) {
            "Minggu" -> {
                val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                starts = (0..6).map { monday.plusDays(it.toLong()) }
                labels = listOf("S", "S", "R", "K", "J", "S", "M")
                end = monday.plusDays(7)
            }
            "Bulan" -> {
                val first = today.withDayOfMonth(1)
                starts = (0..4).map { first.plusDays(it.toLong() * 7) }
                labels = (1..5).map { "P$it" }
                end = first.plusMonths(1)
            }
            "6 bulan" -> {
                val first = today.withDayOfMonth(1).minusMonths(5)
                starts = (0..5).map { first.plusMonths(it.toLong()) }
                labels = starts.map { it.monthValue.toString() }
                end = first.plusMonths(6)
            }
            else -> {
                val first = today.withDayOfYear(1)
                starts = (0..11).map { first.plusMonths(it.toLong()) }
                labels = listOf("J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")
                end = first.plusYears(1)
            }
        }
        val eligible = history.filter { it.date >= starts.first() && it.date < end && it.date <= today }
        val buckets = starts.mapIndexed { index, start ->
            val until = starts.getOrNull(index + 1) ?: end
            val matching = eligible.filter { it.date >= start && it.date < until }
            StatsBucket(matching.sumOf { it.durationMs }, matching.sumOf { it.words })
        }
        val totalSamples = eligible.sumOf { it.samples }
        return StatsSummary(
            durationMs = eligible.sumOf { it.durationMs },
            words = eligible.sumOf { it.words },
            savedMs = eligible.sumOf { it.savedMs },
            averageSpeed = if (totalSamples > 0L) eligible.sumOf { it.speedSum }.toFloat() /
                totalSamples / 1000f else 1f,
            buckets = buckets, labels = labels
        )
    }
}
