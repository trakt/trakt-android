package tv.trakt.trakt.core.home.sections.watchlist

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem

@Immutable
internal data class HomeWatchlistState(
    val items: ImmutableList<WatchlistItem.MovieItem>? = null,
    val navigateMovie: TraktId? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val info: StringResource? = null,
    val error: Exception? = null,
)
