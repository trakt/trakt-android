package tv.trakt.app.tv.core.lists

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.app.tv.core.movies.model.Movie
import tv.trakt.app.tv.core.shows.model.Show

@Immutable
internal data class ListsState(
    val watchlistMovies: ImmutableList<Movie>? = null,
    val watchlistShows: ImmutableList<Show>? = null,
    val isLoading: Boolean = true,
    val error: Exception? = null,
)
