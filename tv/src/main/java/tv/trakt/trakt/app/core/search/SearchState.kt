package tv.trakt.trakt.app.core.search

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.movies.model.Movie
import tv.trakt.trakt.app.core.shows.model.Show

@Immutable
internal data class SearchState(
    val state: State = State.IDLE,
    val trendingResult: SearchResult? = null,
    val recentsResult: SearchResult? = null,
    val searchResult: SearchResult? = null,
    val navigateShow: Show? = null,
    val navigateMovie: Movie? = null,
    val backgroundUrl: String? = null,
    val searching: Boolean = false,
    val error: Exception? = null,
) {
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
