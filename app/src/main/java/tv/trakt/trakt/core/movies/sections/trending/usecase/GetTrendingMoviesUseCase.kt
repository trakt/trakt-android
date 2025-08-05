package tv.trakt.trakt.core.movies.sections.trending.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.core.movies.model.WatchersMovie

internal class GetTrendingMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
) {
    suspend fun getTrendingMovies(): ImmutableList<WatchersMovie> {
        return remoteSource.getTrending(20)
            .asyncMap {
                WatchersMovie(
                    watchers = it.watchers,
                    movie = Movie.fromDto(it.movie),
                )
            }
            .toImmutableList()
            .also { movies ->
//                localSource.upsertMovies(movies.map { it.movie })
            }
    }
}
