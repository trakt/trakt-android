package tv.trakt.trakt.core.lists.sections.watchlist.features.context.movies

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.model.User

@Immutable
internal data class WatchlistMovieContextState(
    val loadingWatched: LoadingState = IDLE,
    val loadingWatchlist: LoadingState = IDLE,
    val user: User? = null,
    val error: Exception? = null,
)
