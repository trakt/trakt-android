package tv.trakt.trakt.core.profile.data.remote

import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.fromDto

internal class UserApiClient(
    private val api: UsersApi,
) : UserRemoteDataSource {
    override suspend fun getProfile(): User {
        val response = api.getUsersSettings(
            extended = "browsing",
        ).body()

        return User.fromDto(response)
    }
}
