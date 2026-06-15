package tv.trakt.trakt.core.discover

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.user.UserCollectionState
import tv.trakt.trakt.helpers.editscreen.data.model.EditScreenKey

@Immutable
internal data class DiscoverState(
    val visibility: ImmutableMap<EditScreenKey, Boolean>? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val user: UserState = UserState(),
) {
    data class UserState(
        val user: User? = null,
        val loading: LoadingState = LoadingState.Idle,
    ) {
        val isAuthenticated: Boolean
            get() = user != null
    }
}
