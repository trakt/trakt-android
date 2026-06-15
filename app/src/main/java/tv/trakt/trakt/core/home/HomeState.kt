package tv.trakt.trakt.core.home

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.helpers.editscreen.data.model.EditScreenKey

@Immutable
internal data class HomeState(
    val mode: MediaMode? = null,
    val user: UserState = UserState(),
    val visibility: ImmutableMap<EditScreenKey, Boolean>? = null,
) {
    data class UserState(
        val user: User? = null,
        val loading: LoadingState = LoadingState.Idle,
    ) {
        val isAuthenticated: Boolean
            get() = user != null
    }
}
