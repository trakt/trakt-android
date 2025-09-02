package tv.trakt.trakt.core.search

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User

@Immutable
internal data class SearchState(
    val state: State = State.IDLE,
    val trendingResult: SearchResult? = null,
    val recentsResult: SearchResult? = null,
    val searchResult: SearchResult? = null,
    val navigateShow: Show? = null,
    val navigateMovie: Movie? = null,
    val backgroundUrl: String? = null,
    val user: UserState = UserState(),
    val searching: Boolean = false,
    val error: Exception? = null,
) {
    data class UserState(
        val user: User? = null,
        val loading: LoadingState = LoadingState.IDLE,
    ) {
        val isAuthenticated: Boolean
            get() = user != null
    }

    data class SearchResult(
        val shows: ImmutableList<Show>? = null,
        val movies: ImmutableList<Movie>? = null,
    )

    enum class State {
        IDLE,
        RECENTS,
        TRENDING,
        SEARCH_RESULTS,
        ERROR,
    }
}
