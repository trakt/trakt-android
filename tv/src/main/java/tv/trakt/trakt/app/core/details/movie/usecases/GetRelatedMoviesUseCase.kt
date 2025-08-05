package tv.trakt.trakt.app.core.details.movie.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.app.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

internal class GetRelatedMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localSource: MovieLocalDataSource,
) {
    suspend fun getRelatedMovies(movieId: TraktId): ImmutableList<Movie> {
        return remoteSource.getRelatedMovies(movieId)
            .asyncMap { Movie.fromDto(it) }
            .also { movies ->
                localSource.upsertMovies(movies)
            }.toImmutableList()
    }
}
