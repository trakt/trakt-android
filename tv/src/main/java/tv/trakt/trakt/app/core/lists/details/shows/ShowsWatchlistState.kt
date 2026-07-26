package tv.trakt.trakt.app.core.lists.details.shows

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.lists.filters.TvListFilterConfiguration
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.sorting.Sorting

@Immutable
internal data class ShowsWatchlistState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val shows: ImmutableList<Show>? = null,
    val error: Exception? = null,
    val filter: GlobalFilter = TvListFilterConfiguration.ShowsWatchlist.defaultFilter,
    val sorting: Sorting = Sorting.RecentlyAdded,
)
