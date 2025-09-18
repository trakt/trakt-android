package tv.trakt.trakt.core.movies.ui.context

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.IDLE

@Immutable
internal data class MovieContextState(
    val isWatched: Boolean = false,
    val isWatchlist: Boolean = false,
    val loadingWatched: LoadingState = IDLE,
    val loadingWatchlist: LoadingState = IDLE,
    val error: Exception? = null,
)
