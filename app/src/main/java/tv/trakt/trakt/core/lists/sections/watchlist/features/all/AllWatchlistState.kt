package tv.trakt.trakt.core.lists.sections.watchlist.features.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.main.model.MediaMode

@Immutable
internal data class AllWatchlistState(
    val backgroundUrl: String? = null,
    val filter: MediaMode? = null,
    val items: ImmutableList<WatchlistItem>? = null,
    val navigateShow: TraktId? = null,
    val navigateMovie: TraktId? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val info: StringResource? = null,
    val error: Exception? = null,
    val isHomeWatchlist: Boolean = false,
)
