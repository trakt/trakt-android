package tv.trakt.trakt.core.home.sections.activity.views.context

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Idle

@Immutable
internal data class ActivityItemContextState(
    val loadingRemove: LoadingState = Idle,
    val loadingWatchlist: LoadingState = Idle,
    val error: Exception? = null,
)
