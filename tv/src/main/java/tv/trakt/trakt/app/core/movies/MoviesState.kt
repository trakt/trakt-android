package tv.trakt.trakt.app.core.movies

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.movies.model.AnticipatedMovie
import tv.trakt.trakt.app.core.movies.model.TrendingMovie
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.User

@Immutable
internal data class MoviesState(
    val isLoading: Boolean = true,
    val trendingMovies: ImmutableList<TrendingMovie>? = null,
    val popularMovies: ImmutableList<Movie>? = null,
    val anticipatedMovies: ImmutableList<AnticipatedMovie>? = null,
    val recommendedMovies: ImmutableList<Movie>? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val user: User? = null,
    val error: Exception? = null,
)
