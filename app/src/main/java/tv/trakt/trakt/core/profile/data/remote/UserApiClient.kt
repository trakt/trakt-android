package tv.trakt.trakt.core.profile.data.remote

import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.networking.SocialActivityItemDto

internal class UserApiClient(
    private val usersApi: UsersApi,
) : UserRemoteDataSource {
    override suspend fun getProfile(): User {
        val response = usersApi.getUsersSettings(
            extended = "browsing",
        ).body()

        return User.fromDto(response)
    }

    override suspend fun getSocialActivity(
        limit: Int,
        type: String,
    ): List<SocialActivityItemDto> {
        val response = usersApi.getUsersActivities(
            id = "me", // or use the current user ID
            type = type,
            limit = limit,
            extended = "full,cloud9",
            page = 1,
        ).body()

        return response
    }
}
