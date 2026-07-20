package tv.trakt.trakt.core.home

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.User

@Immutable
internal data class HomeState(
    val mode: MediaMode? = null,
    val user: UserState = UserState(),
    val welcomeBanner: Boolean = false,
    val collection: UserCollectionState = UserCollectionState.Default,
) {
    data class UserState(
        val user: User? = null,
        val loading: LoadingState = LoadingState.Idle,
    ) {
        val isAuthenticated: Boolean
            get() = user != null
    }
}
