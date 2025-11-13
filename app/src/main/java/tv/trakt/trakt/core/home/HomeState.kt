package tv.trakt.trakt.core.home

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.main.model.MediaMode

@Immutable
internal data class HomeState(
    val backgroundUrl: String? = null,
    val mode: MediaMode? = null,
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
