package tv.trakt.trakt.common.core.user.data.remote.favorites

import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.common.model.sorting.SortType.Default
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.SyncFavoriteMovieDto
import tv.trakt.trakt.common.networking.SyncFavoriteShowDto

class UserFavoritesApiClient(
    private val usersApi: UsersApi,
) : UserFavoritesRemoteDataSource {
    override suspend fun getFavoriteShows(
        extended: String?,
        sorting: Sorting?,
        userId: String,
    ): List<SyncFavoriteShowDto> {
        val response = usersApi.getUsersFavoritesShows(
            id = userId,
            extended = extended,
            sort = "",
            page = 1,
            limit = 250,
            sortBy = when (sorting?.type) {
                Default, null -> null
                else -> sorting.type.value
            },
            sortHow = sorting?.order?.value,
        )

        return response.body()
    }

    override suspend fun getFavoriteMovies(
        extended: String?,
        sorting: Sorting?,
        userId: String,
    ): List<SyncFavoriteMovieDto> {
        val response = usersApi.getUsersFavoritesMovies(
            id = userId,
            extended = extended,
            sort = "",
            page = 1,
            limit = 250,
            sortBy = when (sorting?.type) {
                Default, null -> null
                else -> sorting.type.value
            },
            sortHow = sorting?.order?.value,
        )

        return response.body()
    }
}
