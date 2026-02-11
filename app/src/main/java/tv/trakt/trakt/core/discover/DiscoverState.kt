package tv.trakt.trakt.core.discover

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.user.UserCollectionState

@Immutable
internal data class DiscoverState(
    val collection: UserCollectionState = UserCollectionState.Default,
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
