package tv.trakt.trakt.core.lists

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.model.User

@Immutable
internal data class ListsState(
    val backgroundUrl: String? = null,
    val user: UserState = UserState(),
) {
    data class UserState(
        val user: User? = null,
        val loading: LoadingState = IDLE,
    ) {
        val isAuthenticated: Boolean
            get() = user != null
    }
}
