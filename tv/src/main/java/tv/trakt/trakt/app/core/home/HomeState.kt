package tv.trakt.trakt.app.core.home

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.User

@Immutable
internal data class HomeState(
    val authentication: AuthenticationState = AuthenticationState.INITIAL,
    val mode: MediaMode = MediaMode.Media,
    val profile: User? = null,
    val backgroundUrl: String? = null,
    val error: Exception? = null,
) {
    enum class AuthenticationState {
        INITIAL,
        LOADING,
        AUTHENTICATED,
        UNAUTHENTICATED,
    }
}
