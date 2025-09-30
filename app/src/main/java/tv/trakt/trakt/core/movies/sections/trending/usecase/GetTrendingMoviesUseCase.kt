package tv.trakt.trakt.core.movies.sections.trending.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.core.movies.model.WatchersMovie
import tv.trakt.trakt.core.movies.sections.trending.data.local.TrendingMoviesLocalDataSource
import java.time.Instant

private const val DEFAULT_LIMIT = 24
internal const val DEFAULT_ALL_LIMIT = 102

internal class GetTrendingMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localTrendingSource: TrendingMoviesLocalDataSource,
    private val localMovieSource: MovieLocalDataSource,
) {
    suspend fun getLocalMovies(): ImmutableList<WatchersMovie> {
        return localTrendingSource.getMovies()
            .sortedByDescending { it.watchers }
            .toImmutableList()
            .also {
                localMovieSource.upsertMovies(
                    it.asyncMap { item -> item.movie },
                )
            }
    }

    suspend fun getMovies(
        limit: Int = DEFAULT_LIMIT,
        page: Int = 1,
        skipLocal: Boolean = false,
    ): ImmutableList<WatchersMovie> {
        return remoteSource.getTrending(page, limit)
            .asyncMap {
                WatchersMovie(
                    watchers = it.watchers,
                    movie = Movie.fromDto(it.movie),
                )
            }
            .toImmutableList()
            .also { movies ->
                if (!skipLocal) {
                    localTrendingSource.addMovies(
                        movies = movies.take(DEFAULT_LIMIT),
                        addedAt = Instant.now(),
                    )
                }

                localMovieSource.upsertMovies(
                    movies.asyncMap { item -> item.movie },
                )
            }
    }
}
