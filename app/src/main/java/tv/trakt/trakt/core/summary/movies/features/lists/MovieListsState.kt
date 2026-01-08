package tv.trakt.trakt.core.summary.movies.features.lists

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.CustomList

@Immutable
internal data class MovieListsState(
    val items: ImmutableList<CustomList>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val collapsed: Boolean? = null,
    val error: Exception? = null,
)
