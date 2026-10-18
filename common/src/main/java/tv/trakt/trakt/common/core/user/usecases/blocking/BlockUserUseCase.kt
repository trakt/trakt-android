package tv.trakt.trakt.common.core.user.usecases.blocking

import tv.trakt.trakt.common.core.user.data.remote.social.UserSocialRemoteDataSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.model.TraktId

/**
 * Use case for blocking and unblocking users.
 */
class BlockUserUseCase(
    private val remoteSource: UserSocialRemoteDataSource,
    private val analytics: Analytics,
) {
    suspend fun blockUser(userId: TraktId) {
        remoteSource.blockUser(userId = userId.value.toString())
        analytics.logBlock(blocked = true)
    }

    suspend fun unblockUser(userId: TraktId) {
        remoteSource.unblockUser(userId = userId.value.toString())
        analytics.logBlock(blocked = false)
    }
}
