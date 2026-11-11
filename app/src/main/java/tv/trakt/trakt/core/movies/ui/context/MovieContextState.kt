package tv.trakt.trakt.core.movies.ui.context

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.model.User

@Immutable
internal data class MovieContextState(
    val isWatched: Boolean = false,
    val isWatchlist: Boolean = false,
    val loadingWatched: LoadingState = Idle,
    val loadingWatchlist: LoadingState = Idle,
    val loadingCheckIn: LoadingState = Idle,
    val loadingList: LoadingState = Idle,
    val user: User? = null,
    val error: Exception? = null,
) {
    val isLoadingOrDone: Boolean
        get() = loadingWatched.isLoading ||
            loadingWatchlist.isLoading ||
            loadingCheckIn.isLoading ||
            loadingList.isLoading ||
            loadingWatchlist.isDone ||
            loadingWatched.isDone ||
            loadingCheckIn.isDone ||
            loadingList.isDone
}
