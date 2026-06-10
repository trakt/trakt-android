package tv.trakt.trakt.core.home.sections.streaks.data

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.core.home.sections.streaks.model.MonthlyStreakData
import java.time.LocalDate

internal interface StreaksManager {
    suspend fun loadStreakData(
        localDay: LocalDate,
        mode: MediaMode,
    )

    fun observeStreakData(): Flow<MonthlyStreakData>
}
