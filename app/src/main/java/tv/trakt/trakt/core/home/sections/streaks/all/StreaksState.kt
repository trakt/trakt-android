package tv.trakt.trakt.core.home.sections.streaks.all

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.core.home.sections.streaks.model.MonthlyStreakData

@Immutable
internal data class StreaksState(
    val mode: MediaMode? = null,
    val data: MonthlyStreakData? = null,
)
