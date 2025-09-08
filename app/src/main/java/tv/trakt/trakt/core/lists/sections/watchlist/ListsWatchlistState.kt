package tv.trakt.trakt.core.lists.sections.watchlist

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistFilter
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem

@Immutable
internal data class ListsWatchlistState(
    val user: User? = null,
    val filter: WatchlistFilter = WatchlistFilter.MEDIA,
    val items: ImmutableList<WatchlistItem>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
