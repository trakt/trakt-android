package tv.trakt.trakt.common.core.user.data.remote.social

import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.networking.SocialActivityItemDto
import tv.trakt.trakt.common.networking.UserCommentsDto
import java.time.ZonedDateTime

class UserSocialApiClient(
    private val usersApi: UsersApi,
) : UserSocialRemoteDataSource {
    override suspend fun getSocialActivity(
        page: Int?,
        limit: Int,
        type: String,
    ): List<SocialActivityItemDto> {
        val response = usersApi.getUsersActivities(
            id = "me",
            type = type,
            page = page,
            limit = limit,
            extended = "full,cloud9,colors,rating",
        ).body()

        return response
    }

    override suspend fun getFollowers(): Map<UserCommentsDto, ZonedDateTime> {
        val response = usersApi.getUsersFollowers(
            id = "me",
            extended = "full,vip",
        ).body()

        return response.associate {
            val followedAt = it.followedAt.toZonedDateTime()
            it.user to followedAt
        }
    }

    override suspend fun getFollowing(): Map<UserCommentsDto, ZonedDateTime> {
        val response = usersApi.getUsersFollowing(
            id = "me",
            extended = "full,vip",
        ).body()

        return response.associate {
            val followedAt = it.followedAt.toZonedDateTime()
            it.user to followedAt
        }
    }
}
