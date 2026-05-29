package tv.trakt.trakt.common.core.user.data.remote.favorites

import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.SyncFavoriteMovieDto
import tv.trakt.trakt.common.networking.SyncFavoriteShowDto

interface UserFavoritesRemoteDataSource {
    suspend fun getFavoriteShows(
        extended: String? = null,
        sorting: Sorting? = null,
        userId: String = "me",
    ): List<SyncFavoriteShowDto>

    suspend fun getFavoriteMovies(
        extended: String? = null,
        sorting: Sorting? = null,
        userId: String = "me",
    ): List<SyncFavoriteMovieDto>
}
