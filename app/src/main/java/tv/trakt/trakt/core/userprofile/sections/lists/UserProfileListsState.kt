package tv.trakt.trakt.core.userprofile.sections.lists

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType

@Immutable
internal data class UserProfileListsState(
    val items: ImmutableList<CustomList>? = null,
    val filter: PersonalListType = PersonalListType.Personal,
    val loading: LoadingState = Idle,
    val error: Exception? = null,
)
