package tv.trakt.trakt.core.movies.sections.recommended.usecase.recommended

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.core.movies.sections.recommended.data.local.RecommendedMoviesLocalDataSource
import tv.trakt.trakt.core.movies.sections.recommended.usecase.GetRecommendedMoviesUseCase
import java.time.Instant

private const val DEFAULT_LIMIT = 24

internal class DefaultGetRecommendedMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localRecommendedSource: RecommendedMoviesLocalDataSource,
    private val localMovieSource: MovieLocalDataSource,
) : GetRecommendedMoviesUseCase {
    override suspend fun getLocalMovies(): ImmutableList<Movie> {
        return localRecommendedSource.getMovies()
            .toImmutableList()
            .also {
                localMovieSource.upsertMovies(it)
            }
    }

    override suspend fun getMovies(
        limit: Int,
        skipLocal: Boolean,
    ): ImmutableList<Movie> {
        return remoteSource.getRecommended(
            limit = limit,
        )
            .asyncMap {
                Movie.fromDto(it)
            }
            .toImmutableList()
            .also { movies ->
                if (!skipLocal) {
                    localRecommendedSource.addMovies(
                        movies = movies.take(DEFAULT_LIMIT),
                        addedAt = Instant.now(),
                    )
                }

                localMovieSource.upsertMovies(movies)
            }
    }
}
