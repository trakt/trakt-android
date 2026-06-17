package tv.trakt.trakt.core.profile.sections.screentime.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import tv.trakt.trakt.resources.R
import java.time.LocalDate
import java.time.LocalTime
import kotlin.time.Duration

@Immutable
internal data class ScreenTimeData(
    val stats: Stats,
    val dailyHours: ImmutableMap<LocalDate, Duration>,
    val peakHours: ImmutableMap<PeakHour, Int>,
) {
    data class Stats(
        val totalTime: Duration,
        val totalTimeChange: Duration,
        val averagePerDay: Duration,
        val averagePerDayChange: Duration,
        val showsTime: Duration,
        val showsTimeChange: Duration,
        val moviesTime: Duration,
        val moviesTimeChange: Duration,
        val wakingHoursPercent: Int,
        val wakingHoursPercentChange: Int,
    )

    enum class PeakHour(
        @param:StringRes val displayRes: Int,
        val period: Pair<LocalTime, LocalTime>,
    ) {
        Morning(R.string.text_stats_time_morning, LocalTime.of(5, 0) to LocalTime.of(11, 59)),
        Afternoon(R.string.text_stats_time_afternoon, LocalTime.of(12, 0) to LocalTime.of(16, 59)),
        Evening(R.string.text_stats_time_evening, LocalTime.of(17, 0) to LocalTime.of(21, 59)),
        Night(R.string.text_stats_time_late_night, LocalTime.of(22, 0) to LocalTime.of(4, 59)),
    }
}
