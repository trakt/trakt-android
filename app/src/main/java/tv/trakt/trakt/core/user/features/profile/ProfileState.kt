package tv.trakt.trakt.core.user.features.profile

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User

@Immutable
internal data class ProfileState(
    val user: User? = null,
    val backgroundUrl: String? = null,
    val loading: LoadingState = LoadingState.IDLE,
)
