package tv.trakt.trakt.common.core.user.data.remote.social

import org.openapitools.client.apis.UsersApi
import org.openapitools.client.models.PostUsersFollow201Response
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.networking.SocialActivityItemDto
import tv.trakt.trakt.common.networking.UserBlockedDto
import tv.trakt.trakt.common.networking.UserCommentsDto
import tv.trakt.trakt.common.networking.UserFollowRequestDto
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider
import java.time.Instant
import java.time.ZonedDateTime

class UserSocialApiClient(
    private val usersApi: UsersApi,
    private val cacheMarker: CacheMarkerProvider,
) : UserSocialRemoteDataSource {
    override suspend fun getSocialActivity(
        page: Int?,
        limit: Int,
        type: String,
        filters: GlobalFilter?,
    ): List<SocialActivityItemDto> {
        val response = usersApi.getUsersActivities(
            id = "me",
            type = type,
            page = page,
            limit = limit,
            extended = "full,cloud9,colors,rating,vip",
            watchnow = filters?.availability?.joinToString(",") { it.slug },
            genres = filters?.genre?.joinToString(",") { it.slug },
            subgenres = filters?.subgenre?.joinToString(","),
            years = filters?.years?.let { "${it.first}-${it.second}" },
            ratings = filters?.rating?.let { "${it.first}-${it.second}" },
            runtimes = filters?.runtime?.let { "${it.first}-${it.second}" },
            countries = filters?.countries?.joinToString(",") ?: filters?.region?.slug,
            certifications = filters?.certification?.joinToString(",") { it.slug },
            startDate = null,
            endDate = null,
            ignoreWatched = filters?.hideWatched,
            ignoreWatchlisted = filters?.hideWatchlist,
            ignoreCollected = null,
        ).body()

        return response
    }

    override suspend fun getFollowers(
        userId: String,
        extended: String?,
    ): Map<UserCommentsDto, ZonedDateTime> {
        val response = usersApi.getUsersFollowers(
            id = userId,
            extended = extended,
        ).body()

        return response
            .sortedByDescending { it.followedAt }
            .associate {
                val followedAt = it.followedAt.toZonedDateTime()
                it.user to followedAt
            }
    }

    override suspend fun getFollowing(
        userId: String,
        extended: String?,
    ): Map<UserCommentsDto, ZonedDateTime> {
        val response = usersApi.getUsersFollowing(
            id = userId,
            extended = extended,
        ).body()

        return response
            .sortedByDescending { it.followedAt }
            .associate {
                val followedAt = it.followedAt.toZonedDateTime()
                it.user to followedAt
            }
    }

    override suspend fun getRequests(extended: String?): List<UserFollowRequestDto> {
        val response = usersApi.getUsersRequestsFollow(
            extended = extended,
        ).body()

        return response
            .sortedByDescending { it.requestedAt }
    }

    override suspend fun approveRequest(requestId: Int) {
        usersApi.postUsersRequestsApprove(
            id = requestId.toString(),
            extended = null,
            body = null,
        ).also {
            cacheMarker.invalidate()
        }
    }

    override suspend fun rejectRequest(requestId: Int) {
        usersApi.deleteUsersRequestsDeny(
            id = requestId.toString(),
        ).also {
            cacheMarker.invalidate()
        }
    }

    override suspend fun followUser(userId: String): PostUsersFollow201Response {
        return usersApi.postUsersFollow(
            id = userId,
            body = null,
        )
            .body()
            .also {
                cacheMarker.invalidate()
            }
    }

    override suspend fun unfollowUser(userId: String) {
        usersApi.deleteUsersUnfollow(
            id = userId,
        ).also {
            cacheMarker.invalidate()
        }
    }

    override suspend fun getBlockedUsers(): Map<UserBlockedDto, Instant> {
        return usersApi.getUsersBlocked().body()
            .sortedByDescending { it.blockedAt }
            .associate {
                val blockedAt = it.blockedAt.toInstant()
                it.user to blockedAt
            }
    }

    override suspend fun blockUser(userId: String) {
        usersApi.postUsersBlock(
            id = userId,
            body = null,
        ).also {
            cacheMarker.invalidate()
        }
    }

    override suspend fun unblockUser(userId: String) {
        usersApi.deleteUsersUnblock(
            id = userId,
        ).also {
            cacheMarker.invalidate()
        }
    }
}
