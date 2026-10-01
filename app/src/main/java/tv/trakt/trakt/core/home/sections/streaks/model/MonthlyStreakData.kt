package tv.trakt.trakt.core.home.sections.streaks.model

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import java.time.LocalDate

internal data class MonthlyStreakData(
    val activity: ImmutableMap<LocalDate, StreakDataPoint>,
    val weekActiveDays: ImmutableSet<LocalDate>,
    val currentStreakTotal: Int,
    val previousStreakTotal: Int,
    val currentStreak: Int,
    val previousStreak: Int,
    val droppedStreaks: Int,
    val activeDaysMonth: Int,
    val activeDaysMonthPercent: Int,
    val activeDaysYear: Int,
) {
    data class StreakDataPoint(
        val episodes: Int,
        val movies: Int,
    ) {
        val total: Int
            get() = episodes + movies
    }
}
