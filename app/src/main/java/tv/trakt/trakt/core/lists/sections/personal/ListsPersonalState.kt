package tv.trakt.trakt.core.lists.sections.personal

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.lists.model.PersonalListItem

@Immutable
internal data class ListsPersonalState(
    val user: User? = null,
    val items: ImmutableList<PersonalListItem>? = null,
    val loading: LoadingState = IDLE,
    val error: Exception? = null,
)
