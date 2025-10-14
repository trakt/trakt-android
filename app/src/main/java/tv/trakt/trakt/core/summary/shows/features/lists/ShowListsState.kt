package tv.trakt.trakt.core.summary.shows.features.lists

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.CustomList

@Immutable
internal data class ShowListsState(
    val items: ImmutableList<CustomList>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
