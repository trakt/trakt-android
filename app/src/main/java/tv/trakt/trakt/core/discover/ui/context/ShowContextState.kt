package tv.trakt.trakt.core.discover.ui.context

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.model.User

@Immutable
internal data class ShowContextState(
    val isWatched: Boolean = false,
    val isWatchlist: Boolean = false,
    val loadingWatched: LoadingState = IDLE,
    val loadingWatchlist: LoadingState = IDLE,
    val user: User? = null,
    val error: Exception? = null,
)
