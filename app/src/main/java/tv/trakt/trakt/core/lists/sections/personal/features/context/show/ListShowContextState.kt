package tv.trakt.trakt.core.lists.sections.personal.features.context.show

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Idle

@Immutable
internal data class ListShowContextState(
    val isWatched: Boolean = false,
    val isWatchlist: Boolean = false,
    val loadingWatched: LoadingState = Idle,
    val loadingWatchlist: LoadingState = Idle,
    val loadingList: LoadingState = Idle,
    val error: Exception? = null,
)
