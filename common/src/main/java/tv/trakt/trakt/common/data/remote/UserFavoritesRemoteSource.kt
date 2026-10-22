package tv.trakt.trakt.common.data.remote

import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.SortType.Rank
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.SyncFavoriteMovieDto
import tv.trakt.trakt.common.networking.SyncFavoriteShowDto

class UserFavoritesRemoteSource(
    private val usersApi: UsersApi,
) {
    suspend fun getFavoriteShows(
        extended: String? = null,
        sorting: Sorting? = null,
        userId: String = "me",
        pagination: Pagination = Pagination(page = 1, limit = 250),
    ): List<SyncFavoriteShowDto> {
        val response = usersApi.getUsersFavoritesShows(
            id = userId,
            extended = extended,
            sort = "",
            page = pagination.page,
            limit = pagination.limit,
            sortBy = when (sorting?.type) {
                Rank, null -> null
                else -> sorting.type.value
            },
            sortHow = sorting?.order?.value,
        )

        return response.body()
    }

    suspend fun getFavoriteMovies(
        extended: String? = null,
        sorting: Sorting? = null,
        userId: String = "me",
        pagination: Pagination = Pagination(page = 1, limit = 250),
    ): List<SyncFavoriteMovieDto> {
        val response = usersApi.getUsersFavoritesMovies(
            id = userId,
            extended = extended,
            sort = "",
            page = pagination.page,
            limit = pagination.limit,
            sortBy = when (sorting?.type) {
                Rank, null -> null
                else -> sorting.type.value
            },
            sortHow = sorting?.order?.value,
        )

        return response.body()
    }
}
