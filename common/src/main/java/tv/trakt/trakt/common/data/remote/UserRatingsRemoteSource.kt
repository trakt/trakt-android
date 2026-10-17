package tv.trakt.trakt.common.data.remote

import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.networking.UserRatingDto

class UserRatingsRemoteSource(
    private val usersApi: UsersApi,
) {
    suspend fun getRatingsShows(): List<UserRatingDto> {
        val response = usersApi.getUsersRatingsShows(
            id = "me",
            extended = null,
            page = null,
            limit = null,
        )

        return response.body()
    }

    suspend fun getRatingsMovies(): List<UserRatingDto> {
        val response = usersApi.getUsersRatingsMovies(
            id = "me",
            extended = null,
            page = null,
            limit = null,
        )

        return response.body()
    }

    suspend fun getRatingsEpisodes(): List<UserRatingDto> {
        val response = usersApi.getUsersRatingsEpisodes(
            id = "me",
            extended = null,
            page = null,
            limit = null,
        )

        return response.body()
    }

    suspend fun getRatingsSeasons(): List<UserRatingDto> {
        val response = usersApi.getUsersRatingsTypedRating(
            id = "me",
            type = "seasons",
            rating = "",
            extended = null,
            page = null,
            limit = null,
        )

        return response.body()
    }

    suspend fun getAllRatings(pagination: Pagination): List<UserRatingDto> {
        val response = usersApi.getUsersRatingsAll(
            id = "me",
            extended = "full,images,colors",
            page = pagination.page,
            limit = pagination.limit,
        )

        return response.body()
    }
}
