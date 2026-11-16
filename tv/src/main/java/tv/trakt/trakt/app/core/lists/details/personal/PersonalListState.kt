package tv.trakt.trakt.app.core.lists.details.personal

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.model.sorting.Sorting

@Immutable
internal data class PersonalListState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val items: ImmutableList<ListMediaItem>? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val sorting: Sorting = Sorting.Default,
    val itemsSorting: Sorting = sorting,
    val error: Exception? = null,
)
