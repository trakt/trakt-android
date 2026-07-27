package tv.trakt.trakt.common.core.user.data.remote.ratings

import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.networking.UserRatingDto

class UserRatingsApiClient(
    private val usersApi: UsersApi,
) : UserRatingsRemoteDataSource {
    override suspend fun getRatingsShows(): List<UserRatingDto> {
        val response = usersApi.getUsersRatingsShows(
            id = "me",
            extended = null,
            page = null,
            limit = null,
        )

        return response.body()
    }

    override suspend fun getRatingsMovies(): List<UserRatingDto> {
        val response = usersApi.getUsersRatingsMovies(
            id = "me",
            extended = null,
            page = null,
            limit = null,
        )

        return response.body()
    }

    override suspend fun getRatingsEpisodes(): List<UserRatingDto> {
        val response = usersApi.getUsersRatingsEpisodes(
            id = "me",
            extended = null,
            page = null,
            limit = null,
        )

        return response.body()
    }

    override suspend fun getRatingsSeasons(): List<UserRatingDto> {
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

    override suspend fun getAllRatings(pagination: Pagination): List<UserRatingDto> {
        val response = usersApi.getUsersRatingsAll(
            id = "me",
            extended = "full,cloud9,colors",
            page = pagination.page,
            limit = pagination.limit,
        )

        return response.body()
    }
}
