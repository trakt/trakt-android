package tv.trakt.trakt.core.home.sections.watchlist.views

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.IDLE

@Immutable
internal data class WatchlistItemContextState(
    val loadingWatched: LoadingState = IDLE,
    val loadingWatchlist: LoadingState = IDLE,
    val error: Exception? = null,
)
