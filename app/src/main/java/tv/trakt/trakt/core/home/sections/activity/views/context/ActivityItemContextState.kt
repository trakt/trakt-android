package tv.trakt.trakt.core.home.sections.activity.views.context

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.IDLE

@Immutable
internal data class ActivityItemContextState(
    val loadingRemove: LoadingState = IDLE,
    val loadingWatchlist: LoadingState = IDLE,
    val error: Exception? = null,
)
