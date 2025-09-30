package tv.trakt.trakt.core.summary.movies

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Movie

@Immutable
internal data class MovieDetailsState(
    val movie: Movie? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
