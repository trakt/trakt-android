package tv.trakt.trakt.app.core.profile.sections.favorites.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.profile.sections.favorites.model.FavoriteItem
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.remote.UserFavoritesRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.Sorting

internal class GetFavoriteMoviesUseCase(
    private val remoteUserSource: UserFavoritesRemoteSource,
    private val localMoviesSource: MovieLocalSource,
) {
    suspend fun getFavoriteMovies(
        sorting: Sorting,
        page: Int = 1,
        limit: Int,
    ): ImmutableList<FavoriteItem.MovieItem> {
        return remoteUserSource.getFavoriteMovies(
            extended = "full,colors,streaming_ids",
            sorting = sorting,
            pagination = Pagination(page = page, limit = limit),
        )
            .asyncMap {
                FavoriteItem.MovieItem(
                    movie = Movie.fromDto(it.movie),
                    rank = it.rank,
                    listedAt = it.listedAt.toInstant(),
                )
            }
            .toImmutableList()
            .also {
                localMoviesSource.upsertMovies(it.map { item -> item.movie })
            }
    }
}
