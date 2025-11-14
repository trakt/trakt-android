package tv.trakt.trakt.core.lists.sections.personal

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.lists.model.PersonalListItem
import tv.trakt.trakt.core.user.UserCollectionState

@Immutable
internal data class ListsPersonalState(
    val user: User? = null,
    val list: CustomList? = null,
    val items: ImmutableList<PersonalListItem>? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val navigateShow: TraktId? = null,
    val navigateMovie: TraktId? = null,
    val loading: LoadingState = IDLE,
    val error: Exception? = null,
)
