package tv.trakt.trakt.app.core.lists.details.personal

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.lists.details.personal.model.PersonalListItem

@Immutable
internal data class PersonalListState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val items: ImmutableList<PersonalListItem>? = null,
    val error: Exception? = null,
)
