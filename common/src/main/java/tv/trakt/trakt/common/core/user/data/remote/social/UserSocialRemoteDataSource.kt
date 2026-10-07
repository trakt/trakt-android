package tv.trakt.trakt.common.core.user.data.remote.social

import org.openapitools.client.models.PostUsersFollow201Response
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.networking.SocialActivityItemDto
import tv.trakt.trakt.common.networking.UserBlockedDto
import tv.trakt.trakt.common.networking.UserCommentsDto
import tv.trakt.trakt.common.networking.UserFollowRequestDto
import tv.trakt.trakt.common.networking.api.v3.model.V3LeaderboardEntryResponse
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

    suspend fun getRequests(extended: String?): List<UserFollowRequestDto>

    suspend fun getLeaderboard(pagination: Pagination): List<V3LeaderboardEntryResponse>

    suspend fun approveRequest(requestId: Int)

    suspend fun rejectRequest(requestId: Int)

    suspend fun followUser(userId: String): PostUsersFollow201Response

    suspend fun unfollowUser(userId: String)

    suspend fun getBlockedUsers(): Map<UserBlockedDto, Instant>

    suspend fun blockUser(userId: String)

    suspend fun unblockUser(userId: String)
}
