package tv.trakt.trakt.core.movies.sections.recommended.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.core.movies.sections.recommended.data.local.RecommendedMoviesLocalDataSource
import java.time.Instant

private const val DEFAULT_LIMIT = 24
internal const val DEFAULT_ALL_LIMIT = 200

internal class GetRecommendedMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localRecommendedSource: RecommendedMoviesLocalDataSource,
) {
    suspend fun getLocalMovies(): ImmutableList<Movie> {
        return localRecommendedSource.getMovies()
            .toImmutableList()
    }

    suspend fun getMovies(
        limit: Int = DEFAULT_LIMIT,
        skipLocal: Boolean = false,
    ): ImmutableList<Movie> {
        return remoteSource.getRecommended(limit)
            .asyncMap {
                Movie.fromDto(it)
            }
            .toImmutableList()
            .also { movies ->
                if (skipLocal) return@also
                localRecommendedSource.addMovies(
                    movies = movies.take(DEFAULT_LIMIT),
                    addedAt = Instant.now(),
                )
            }
    }
}
