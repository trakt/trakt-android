package tv.trakt.trakt.app.core.movies.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.app.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.app.core.movies.model.AnticipatedMovie
import tv.trakt.trakt.app.core.movies.model.Movie
import tv.trakt.trakt.app.core.movies.model.fromDto
import tv.trakt.trakt.app.helpers.extensions.asyncMap

internal class GetAnticipatedMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localSource: MovieLocalDataSource,
) {
    suspend fun getAnticipatedMovies(): ImmutableList<AnticipatedMovie> {
        return remoteSource.getAnticipatedMovies()
            .asyncMap {
                AnticipatedMovie(
                    listCount = it.listCount,
                    movie = Movie.fromDto(it.movie),
                )
            }
            .toImmutableList()
            .also { movies ->
                localSource.upsertMovies(movies.map { it.movie })
            }
    }
}
