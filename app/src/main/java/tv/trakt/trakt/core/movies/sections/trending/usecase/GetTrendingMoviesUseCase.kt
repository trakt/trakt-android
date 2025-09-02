package tv.trakt.trakt.core.movies.sections.trending.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.core.movies.model.WatchersMovie
import tv.trakt.trakt.core.movies.sections.trending.data.local.TrendingMoviesLocalDataSource
import java.time.Instant

internal class GetTrendingMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localTrendingSource: TrendingMoviesLocalDataSource,
) {
    suspend fun getLocalMovies(): ImmutableList<WatchersMovie> {
        return localTrendingSource.getMovies()
            .sortedByDescending { it.watchers }
            .toImmutableList()
    }

    suspend fun getMovies(limit: Int = 20): ImmutableList<WatchersMovie> {
        return remoteSource.getTrending(limit)
            .asyncMap {
                WatchersMovie(
                    watchers = it.watchers,
                    movie = Movie.fromDto(it.movie),
                )
            }
            .toImmutableList()
            .also { movies ->
                localTrendingSource.addMovies(
                    movies = movies,
                    addedAt = Instant.now(),
                )
            }
    }
}
