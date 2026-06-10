package tv.trakt.trakt.core.home.sections.streaks

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.core.home.sections.streaks.model.MonthlyStreakData

@Immutable
internal data class HomeStreaksState(
    val data: MonthlyStreakData? = null,
)
