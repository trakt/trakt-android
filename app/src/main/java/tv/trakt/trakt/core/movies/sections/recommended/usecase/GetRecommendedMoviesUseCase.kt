package tv.trakt.trakt.core.movies.sections.recommended.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.core.movies.sections.recommended.data.local.RecommendedMoviesLocalDataSource
import java.time.Instant

internal class GetRecommendedMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localRecommendedSource: RecommendedMoviesLocalDataSource,
) {
    suspend fun getLocalMovies(): ImmutableList<Movie> {
        return localRecommendedSource.getMovies()
            .toImmutableList()
    }

    suspend fun getMovies(): ImmutableList<Movie> {
        return remoteSource.getRecommended(20)
            .asyncMap {
                Movie.fromDto(it)
            }
            .toImmutableList()
            .also { movies ->
                localRecommendedSource.addMovies(
                    movies = movies,
                    addedAt = Instant.now(),
                )
            }
    }
}
