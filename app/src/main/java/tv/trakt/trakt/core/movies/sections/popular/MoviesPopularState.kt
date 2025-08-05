package tv.trakt.trakt.core.movies.sections.popular

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Movie

@Immutable
internal data class MoviesPopularState(
    val items: ImmutableList<Movie>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
