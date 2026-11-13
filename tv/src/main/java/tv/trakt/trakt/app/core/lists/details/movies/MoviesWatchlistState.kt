package tv.trakt.trakt.app.core.lists.details.movies

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.sorting.Sorting

@Immutable
internal data class MoviesWatchlistState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val movies: ImmutableList<ListMediaItem.MovieItem>? = null,
    val itemCount: Int? = null,
    val sorting: Sorting = Sorting.RecentlyAdded,
    val itemsSorting: Sorting = sorting,
    val error: Exception? = null,
)
