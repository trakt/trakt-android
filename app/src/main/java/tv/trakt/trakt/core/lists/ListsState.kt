package tv.trakt.trakt.core.lists

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.lists.ListsItem
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType

@Immutable
internal data class ListsState(
    val user: UserState = UserState(),
    val filter: PersonalListType = PersonalListType.Smart,
    val lists: ImmutableList<ListsItem>? = null,
    val listsLoading: LoadingState = Idle,
    val info: DynamicStringResource? = null,
    val error: Exception? = null,
) {
    data class UserState(
        val user: User? = null,
        val loading: LoadingState = Idle,
    ) {
        val isAuthenticated: Boolean
            get() = user != null
    }
}
