package tv.trakt.trakt.common.core.user.data.remote.social

import org.openapitools.client.models.PostUsersFollow201Response
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.networking.SocialActivityItemDto
import tv.trakt.trakt.common.networking.UserBlockedDto
import tv.trakt.trakt.common.networking.UserCommentsDto
import java.time.Instant
import java.time.ZonedDateTime

interface UserSocialRemoteDataSource {
    suspend fun getSocialActivity(
        page: Int? = null,
        limit: Int,
        type: String,
        filters: GlobalFilter?,
    ): List<SocialActivityItemDto>

    suspend fun getFollowing(
        userId: String,
        extended: String?,
    ): Map<UserCommentsDto, ZonedDateTime>

    suspend fun getFollowers(
        userId: String,
        extended: String?,
    ): Map<UserCommentsDto, ZonedDateTime>

    suspend fun followUser(userId: String): PostUsersFollow201Response

    suspend fun unfollowUser(userId: String)

    suspend fun getBlockedUsers(): Map<UserBlockedDto, Instant>

    suspend fun blockUser(userId: String)

    suspend fun unblockUser(userId: String)
}
