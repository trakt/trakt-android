package tv.trakt.trakt.core.movies.sections.popular.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource

internal class GetPopularMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
) {
    suspend fun getPopularMovies(): ImmutableList<Movie> {
        return remoteSource.getPopular(20)
            .asyncMap {
                Movie.fromDto(it)
            }
            .toImmutableList()
            .also {
//                localSource.upsertMovies(it)
            }
    }
}
