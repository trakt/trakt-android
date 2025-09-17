package tv.trakt.trakt.core.home.sections.upnext.views

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.IDLE

@Immutable
internal data class UpNextItemContextState(
    val loadingWatched: LoadingState = IDLE,
    val loadingDrop: LoadingState = IDLE,
    val error: Exception? = null,
)
