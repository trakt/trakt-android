package tv.trakt.trakt.core.home.sections.watchlist

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.core.lists.model.WatchlistItem
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter

@Immutable
internal data class HomeWatchlistState(
    val filter: GlobalFilter? = null,
    val items: ImmutableList<WatchlistItem>? = null,
    val collapsed: Boolean? = null,
    val navigateShow: TraktId? = null,
    val navigateMovie: TraktId? = null,
    val loading: LoadingState = LoadingState.Idle,
    val info: StringResource? = null,
    val error: Exception? = null,
)
