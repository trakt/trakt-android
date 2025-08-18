package tv.trakt.trakt.app.core.movies

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.movies.model.AnticipatedMovie
import tv.trakt.trakt.app.core.movies.model.TrendingMovie
import tv.trakt.trakt.common.model.Movie

@Immutable
internal data class MoviesState(
    val isLoading: Boolean = true,
    val trendingMovies: ImmutableList<TrendingMovie>? = null,
    val popularMovies: ImmutableList<Movie>? = null,
    val anticipatedMovies: ImmutableList<AnticipatedMovie>? = null,
    val recommendedMovies: ImmutableList<Movie>? = null,
    val error: Exception? = null,
)
