package tv.trakt.trakt.app.core.lists.details.movies

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.movies.model.Movie

@Immutable
internal data class MoviesWatchlistState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val movies: ImmutableList<Movie>? = null,
    val error: Exception? = null,
)
