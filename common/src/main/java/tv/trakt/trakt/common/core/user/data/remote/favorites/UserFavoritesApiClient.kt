package tv.trakt.trakt.common.core.user.data.remote.favorites

import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.common.networking.SyncFavoriteMovieDto
import tv.trakt.trakt.common.networking.SyncFavoriteShowDto

class UserFavoritesApiClient(
    private val usersApi: UsersApi,
) : UserFavoritesRemoteDataSource {
    override suspend fun getFavoriteShows(
        extended: String?,
        sort: String?,
    ): List<SyncFavoriteShowDto> {
        val response = usersApi.getUsersFavoritesShows(
            id = "me",
            extended = extended,
            sort = sort ?: "rank",
            page = 1,
            limit = 1000,
        )

        return response.body()
    }

    override suspend fun getFavoriteMovies(
        extended: String?,
        sort: String?,
    ): List<SyncFavoriteMovieDto> {
        val response = usersApi.getUsersFavoritesMovies(
            id = "me",
            extended = extended,
            sort = sort ?: "rank",
            page = 1,
            limit = 1000,
        )

        return response.body()
    }
}
