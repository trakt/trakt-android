package tv.trakt.trakt.core.lists.features.reorder

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.core.lists.model.CustomListItem

@Immutable
internal data class ListReorderState(
    val list: CustomList? = null,
    val items: ImmutableList<CustomListItem>? = null,
    val initialItemsOrder: ImmutableList<Int>? = null,
    val loading: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
    val done: Boolean = false,
)
