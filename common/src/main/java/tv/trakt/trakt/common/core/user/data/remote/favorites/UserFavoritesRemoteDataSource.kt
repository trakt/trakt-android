package tv.trakt.trakt.common.core.user.data.remote.favorites

import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.SyncFavoriteMovieDto
import tv.trakt.trakt.common.networking.SyncFavoriteShowDto

interface UserFavoritesRemoteDataSource {
    suspend fun getFavoriteShows(
        extended: String? = null,
        sorting: Sorting? = null,
        userId: String = "me",
        pagination: Pagination = Pagination(page = 1, limit = 250),
    ): List<SyncFavoriteShowDto>

    suspend fun getFavoriteMovies(
        extended: String? = null,
        sorting: Sorting? = null,
        userId: String = "me",
        pagination: Pagination = Pagination(page = 1, limit = 250),
    ): List<SyncFavoriteMovieDto>
}
