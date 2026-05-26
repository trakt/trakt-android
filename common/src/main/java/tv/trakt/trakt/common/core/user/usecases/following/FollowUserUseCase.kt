package tv.trakt.trakt.common.core.user.usecases.following

import tv.trakt.trakt.common.core.user.data.remote.social.UserSocialRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.HTTP_ERROR_CONFLICT
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.model.TraktId

class FollowUserUseCase(
    private val remoteSource: UserSocialRemoteDataSource,
) {
    suspend fun followUser(userId: TraktId): Result {
        try {
            val response = remoteSource.followUser(userId.value.toString())
            return when (response.approvedAt) {
                null -> Result.RequestPending
                else -> Result.Approved
            }
        } catch (error: Exception) {
            // If the error is a 409, it means the follow request is pending approval.
            if (error.getHttpCode() == HTTP_ERROR_CONFLICT) {
                return Result.RequestPending
            }
            throw error
        }
    }

    suspend fun unfollowUser(userId: TraktId) {
        remoteSource.unfollowUser(userId.value.toString())
    }

    enum class Result {
        Approved,
        RequestPending,
    }
}
