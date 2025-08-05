package tv.trakt.trakt.core.movies.sections.hot

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.core.movies.model.WatchersMovie

@Immutable
internal data class MoviesHotState(
    val items: ImmutableList<WatchersMovie>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
