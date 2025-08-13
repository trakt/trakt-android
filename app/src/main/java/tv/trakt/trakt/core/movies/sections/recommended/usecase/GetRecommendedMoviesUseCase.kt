package tv.trakt.trakt.core.movies.sections.recommended.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource

internal class GetRecommendedMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
) {
    suspend fun getRecommendedMovies(): ImmutableList<Movie> {
        return remoteSource.getRecommended(20)
            .asyncMap {
                Movie.fromDto(it)
            }
            .toImmutableList()
            .also { movies ->
//                localSource.upsertMovies(movies.map { it.movie })
            }
    }
}
