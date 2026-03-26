package tv.trakt.trakt.core.home.sections.upnext.features.context

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Idle

@Immutable
internal data class UpNextItemContextState(
    val loadingWatched: LoadingState = Idle,
    val loadingDrop: LoadingState = Idle,
    val error: Exception? = null,
)
