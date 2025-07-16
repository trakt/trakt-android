package tv.trakt.app.tv.core.home

import androidx.compose.runtime.Immutable
import tv.trakt.app.tv.common.model.User

@Immutable
internal data class HomeState(
    val authentication: AuthenticationState = AuthenticationState.INITIAL,
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
