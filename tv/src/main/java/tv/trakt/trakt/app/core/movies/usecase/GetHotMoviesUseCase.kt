package tv.trakt.trakt.app.core.movies.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.app.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.app.core.movies.model.TrendingMovie
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto

internal class GetHotMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localSource: MovieLocalDataSource,
) {
    suspend fun getHotMovies(): ImmutableList<TrendingMovie> {
        return remoteSource.getMonthlyHotMovies()
            .asyncMap {
                TrendingMovie(
                    watchers = it.watchers,
                    movie = Movie.fromDto(it.movie),
                )
            }
            .toImmutableList()
            .also { movies ->
                localSource.upsertMovies(movies.map { it.movie })
            }
    }
}
