package tv.trakt.trakt.core.home.sections.watchlist.features.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem

@Immutable
internal data class AllHomeWatchlistState(
    val filter: GlobalFilter? = null,
    val items: ImmutableList<WatchlistItem>? = null,
    val navigateShow: TraktId? = null,
    val navigateMovie: TraktId? = null,
    val loading: LoadingState = LoadingState.Idle,
    val user: User? = null,
    val info: StringResource? = null,
    val error: Exception? = null,
)
