package tv.trakt.trakt.app.core.details.movie.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.media.movies.local.MovieLocalDataSource
import tv.trakt.trakt.common.data.remote.MoviesRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

internal class GetRelatedMoviesUseCase(
    private val remoteSource: MoviesRemoteSource,
    private val localSource: MovieLocalDataSource,
) {
    suspend fun getRelatedMovies(movieId: TraktId): ImmutableList<Movie> {
        return remoteSource.getRelated(
            movieId = movieId,
            limit = 20,
        )
            .asyncMap { Movie.fromDto(it) }
            .also { movies ->
                localSource.upsertMovies(movies)
            }.toImmutableList()
    }
}
