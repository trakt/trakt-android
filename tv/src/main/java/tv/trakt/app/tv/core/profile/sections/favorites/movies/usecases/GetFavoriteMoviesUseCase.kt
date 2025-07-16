package tv.trakt.app.tv.core.profile.sections.favorites.movies.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.app.tv.core.movies.data.local.MovieLocalDataSource
import tv.trakt.app.tv.core.movies.model.Movie
import tv.trakt.app.tv.core.movies.model.fromDto
import tv.trakt.app.tv.core.profile.data.remote.ProfileRemoteDataSource
import tv.trakt.app.tv.helpers.extensions.asyncMap

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
