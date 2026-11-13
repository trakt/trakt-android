package tv.trakt.trakt.app.core.lists.details.shows

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.sorting.Sorting

@Immutable
internal data class ShowsWatchlistState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val shows: ImmutableList<Show>? = null,
    val itemCount: Int? = null,
    val sorting: Sorting = Sorting.RecentlyAdded,
    val error: Exception? = null,
)
