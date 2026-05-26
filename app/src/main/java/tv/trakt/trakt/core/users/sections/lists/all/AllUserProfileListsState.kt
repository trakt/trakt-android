package tv.trakt.trakt.core.users.sections.lists.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType

@Immutable
internal data class AllUserProfileListsState(
    val user: User,
    val items: ImmutableList<CustomList>? = null,
    val filter: PersonalListType = PersonalListType.Personal,
    val loading: LoadingState = Idle,
    val loadingMore: LoadingState = Idle,
    val error: Exception? = null,
) {
    data class User(
        val id: TraktId,
        val name: String,
    )
}
