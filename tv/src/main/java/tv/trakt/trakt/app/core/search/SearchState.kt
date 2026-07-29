package tv.trakt.trakt.app.core.search

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show

@Immutable
internal data class SearchState(
    val state: State = State.IDLE,
    val trendingResult: SearchResult? = null,
    val searchResult: SearchResult? = null,
    val navigateShow: Show? = null,
    val navigateMovie: Movie? = null,
    val backgroundUrl: String? = null,
    val searching: Boolean = false,
    val collection: UserCollectionState = UserCollectionState.Default,
    val error: Exception? = null,
) {
    data class SearchResult(
        val shows: ImmutableList<Show>? = null,
        val movies: ImmutableList<Movie>? = null,
    )

    enum class State {
        IDLE,
        TRENDING,
        SEARCH_RESULTS,
        ERROR,
    }
}
