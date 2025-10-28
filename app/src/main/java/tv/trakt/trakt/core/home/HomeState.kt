package tv.trakt.trakt.core.home

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User

@Immutable
internal data class HomeState(
    val backgroundUrl: String? = null,
    val halloween: Boolean = false,
    val user: UserState = UserState(),
) {
    data class UserState(
        val user: User? = null,
        val loading: LoadingState = LoadingState.IDLE,
    ) {
        val isAuthenticated: Boolean
            get() = user != null
    }
}
