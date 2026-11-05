package tv.trakt.trakt.core.profile.sections.progress.context

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.model.User

@Immutable
internal data class ProgressShowContextState(
    val isWatched: Boolean = false,
    val loadingDrop: LoadingState = Idle,
    val loadingWatched: LoadingState = Idle,
    val user: User? = null,
    val error: Exception? = null,
)
