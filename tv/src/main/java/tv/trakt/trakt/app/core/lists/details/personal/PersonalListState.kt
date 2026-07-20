package tv.trakt.trakt.app.core.lists.details.personal

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.lists.details.personal.model.PersonalListItem
import tv.trakt.trakt.common.core.user.UserCollectionState

@Immutable
internal data class PersonalListState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val items: ImmutableList<PersonalListItem>? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val error: Exception? = null,
)
