package tv.trakt.trakt.core.today.stories.watchlist

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState

@Immutable
internal data class TodayWatchlistState(
    val isWatchlisted: Boolean? = null,
    val loading: LoadingState = LoadingState.Idle,
)
