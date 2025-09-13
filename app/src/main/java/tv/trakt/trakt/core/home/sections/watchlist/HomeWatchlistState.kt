package tv.trakt.trakt.core.home.sections.watchlist

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.core.home.sections.watchlist.model.WatchlistMovie

@Immutable
internal data class HomeWatchlistState(
    val items: ImmutableList<WatchlistMovie>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val info: StringResource? = null,
    val error: Exception? = null,
)
