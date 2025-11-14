package tv.trakt.trakt.core.lists

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.User

@Immutable
internal data class ListsState(
    val user: UserState = UserState(),
    val lists: ImmutableList<CustomList>? = null,
    val listsLoading: LoadingState = IDLE,
    val error: Exception? = null,
) {
    data class UserState(
        val user: User? = null,
        val loading: LoadingState = IDLE,
    ) {
        val isAuthenticated: Boolean
            get() = user != null
    }
}
