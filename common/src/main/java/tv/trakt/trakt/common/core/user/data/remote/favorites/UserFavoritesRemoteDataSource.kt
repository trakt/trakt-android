package tv.trakt.trakt.common.core.user.data.remote.favorites

import tv.trakt.trakt.common.networking.SyncFavoriteMovieDto
import tv.trakt.trakt.common.networking.SyncFavoriteShowDto

interface UserFavoritesRemoteDataSource {
    suspend fun getFavoriteShows(
        extended: String? = null,
        sort: String? = null,
    ): List<SyncFavoriteShowDto>

    suspend fun getFavoriteMovies(
        extended: String? = null,
        sort: String? = null,
    ): List<SyncFavoriteMovieDto>
}
