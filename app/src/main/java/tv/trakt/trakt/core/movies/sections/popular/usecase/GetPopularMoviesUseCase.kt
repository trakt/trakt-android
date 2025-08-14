package tv.trakt.trakt.core.movies.sections.popular.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.core.movies.sections.popular.data.local.PopularMoviesLocalDataSource
import java.time.Instant

internal class GetPopularMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localPopularSource: PopularMoviesLocalDataSource,
) {
    suspend fun getLocalMovies(): ImmutableList<Movie> {
        return localPopularSource.getMovies()
            .toImmutableList()
    }

    suspend fun getMovies(): ImmutableList<Movie> {
        return remoteSource.getPopular(20)
            .asyncMap {
                Movie.fromDto(it)
            }
            .toImmutableList()
            .also { movies ->
                localPopularSource.addMovies(
                    movies = movies,
                    addedAt = Instant.now(),
                )
            }
    }
}
