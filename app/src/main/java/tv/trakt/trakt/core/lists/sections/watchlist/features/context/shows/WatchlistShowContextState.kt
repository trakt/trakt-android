package tv.trakt.trakt.core.lists.sections.watchlist.features.context.shows

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.model.User

@Immutable
internal data class WatchlistShowContextState(
    val loadingWatched: LoadingState = Idle,
    val loadingWatchlist: LoadingState = Idle,
    val user: User? = null,
    val error: Exception? = null,
)
