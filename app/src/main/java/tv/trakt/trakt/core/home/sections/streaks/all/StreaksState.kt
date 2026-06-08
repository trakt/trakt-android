package tv.trakt.trakt.core.home.sections.streaks.all

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.core.home.sections.streaks.model.MonthlyStreakData

@Immutable
internal data class StreaksState(
    val mode: MediaMode? = null,
    val data: MonthlyStreakData? = null,
    val loading: LoadingState = Idle,
    val error: Exception? = null,
)
