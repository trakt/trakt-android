package tv.trakt.trakt.app.core.profile.sections.favorites.movies.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.app.core.movies.model.Movie
import tv.trakt.trakt.app.core.movies.model.fromDto
import tv.trakt.trakt.app.core.profile.data.remote.ProfileRemoteDataSource
import tv.trakt.trakt.app.helpers.extensions.asyncMap

internal class GetFavoriteMoviesUseCase(
    private val remoteUserSource: ProfileRemoteDataSource,
    private val localMoviesSource: MovieLocalDataSource,
) {
    suspend fun getFavoriteMovies(
        page: Int = 1,
        limit: Int,
    ): ImmutableList<Movie> {
        return remoteUserSource.getUserFavoriteMovies(page, limit)
            .asyncMap { Movie.fromDto(it.movie) }
            .toImmutableList()
            .also {
                localMoviesSource.upsertMovies(it)
            }
    }
}
