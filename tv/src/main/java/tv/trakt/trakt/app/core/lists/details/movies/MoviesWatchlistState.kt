package tv.trakt.trakt.app.core.lists.details.movies

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.lists.filters.TvListFilterConfiguration
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.sorting.Sorting

@Immutable
internal data class MoviesWatchlistState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val movies: ImmutableList<Movie>? = null,
    val error: Exception? = null,
    val filter: GlobalFilter = TvListFilterConfiguration.MoviesWatchlist.defaultFilter,
    val sorting: Sorting = Sorting.RecentlyAdded,
)
