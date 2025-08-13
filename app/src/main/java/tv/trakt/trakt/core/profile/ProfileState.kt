package tv.trakt.trakt.core.profile

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User

@Immutable
internal data class ProfileState(
    val loading: LoadingState = LoadingState.IDLE,
    val profile: User? = null,
    val isSignedIn: Boolean = false,
    val backgroundUrl: String? = null,
)
