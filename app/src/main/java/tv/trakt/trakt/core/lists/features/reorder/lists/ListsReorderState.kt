package tv.trakt.trakt.core.lists.features.reorder.lists

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomList

@Immutable
internal data class ListsReorderState(
    val items: ImmutableList<CustomList>? = null,
    val initialItemsOrder: ImmutableList<TraktId>? = null,
    val loading: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
    val done: Boolean = false,
)
