package tv.trakt.trakt.core.lists.sections.watchlist.features.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.ListsMediaFilter
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem

@Immutable
internal data class AllWatchlistState(
    val backgroundUrl: String? = null,
    val filter: ListsMediaFilter? = null,
    val items: ImmutableList<WatchlistItem>? = null,
    val navigateMovie: TraktId? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
    val isHomeWatchlist: Boolean = false,
)
