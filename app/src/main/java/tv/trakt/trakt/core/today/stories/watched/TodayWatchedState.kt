package tv.trakt.trakt.core.today.stories.watched

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState

@Immutable
internal data class TodayWatchedState(
    val isWatched: Boolean = false,
    val loading: LoadingState = LoadingState.Idle,
)
