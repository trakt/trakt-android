package tv.trakt.trakt.core.summary.shows.features.lists

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.TraktId

@Immutable
internal data class ShowListsState(
    val items: ImmutableList<CustomList>? = null,
    val likedItems: ImmutableSet<TraktId>? = null,
    val loading: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
)
