package tv.trakt.trakt.core.movies.sections.hot.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.core.movies.model.WatchersMovie

internal class GetHotMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
) {
    suspend fun getHotMovies(): ImmutableList<WatchersMovie> {
        return remoteSource.getHot(20)
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
