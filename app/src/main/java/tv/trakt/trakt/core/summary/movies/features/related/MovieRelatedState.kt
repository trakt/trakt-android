package tv.trakt.trakt.core.summary.movies.features.related

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Movie

@Immutable
internal data class MovieRelatedState(
    val items: ImmutableList<Movie>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
