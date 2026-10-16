package tv.trakt.trakt.app.core.profile.sections.favorites.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.media.movies.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.user.data.remote.favorites.UserFavoritesRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.Sorting

internal class GetFavoriteMoviesUseCase(
    private val remoteUserSource: UserFavoritesRemoteSource,
    private val localMoviesSource: MovieLocalDataSource,
) {
    suspend fun getFavoriteMovies(
        page: Int = 1,
        limit: Int,
    ): ImmutableList<Movie> {
        return remoteUserSource.getFavoriteMovies(
            extended = "full,cloud9,colors,streaming_ids",
            sorting = Sorting.RecentlyAdded,
            pagination = Pagination(page = page, limit = limit),
        )
            .asyncMap { Movie.fromDto(it.movie) }
            .toImmutableList()
            .also {
                localMoviesSource.upsertMovies(it)
            }
    }
}
