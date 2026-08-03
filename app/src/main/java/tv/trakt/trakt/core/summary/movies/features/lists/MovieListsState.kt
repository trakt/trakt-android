package tv.trakt.trakt.core.summary.movies.features.lists

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomList

@Immutable
internal data class MovieListsState(
    val items: ImmutableList<CustomList>? = null,
    val likedItems: ImmutableSet<TraktId>? = null,
    val loading: LoadingState = LoadingState.Idle,
    val collapsed: Boolean? = null,
    val error: Exception? = null,
)
