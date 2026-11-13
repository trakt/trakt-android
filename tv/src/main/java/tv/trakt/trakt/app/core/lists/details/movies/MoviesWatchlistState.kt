package tv.trakt.trakt.app.core.lists.details.movies

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.sorting.Sorting

@Immutable
internal data class MoviesWatchlistState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val movies: ImmutableList<Movie>? = null,
    val itemCount: Int? = null,
    val sorting: Sorting = Sorting.RecentlyAdded,
    val error: Exception? = null,
)
