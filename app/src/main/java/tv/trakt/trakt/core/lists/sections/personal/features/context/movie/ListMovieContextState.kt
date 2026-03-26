package tv.trakt.trakt.core.lists.sections.personal.features.context.movie

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Idle

@Immutable
internal data class ListMovieContextState(
    val isWatched: Boolean = false,
    val isWatchlist: Boolean = false,
    val loadingWatched: LoadingState = Idle,
    val loadingWatchlist: LoadingState = Idle,
    val loadingCheckIn: LoadingState = Idle,
    val loadingList: LoadingState = Idle,
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
