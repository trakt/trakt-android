package tv.trakt.trakt.common.core.user.usecases.blocking

import tv.trakt.trakt.common.core.user.data.remote.social.UserSocialRemoteDataSource
import tv.trakt.trakt.common.model.TraktId

/**
 * Use case for blocking and unblocking users.
 */
class BlockUserUseCase(
    private val remoteSource: UserSocialRemoteDataSource,
) {
    suspend fun blockUser(userId: TraktId) {
        remoteSource.blockUser(userId = userId.value.toString())
    }

    suspend fun unblockUser(userId: TraktId) {
        remoteSource.unblockUser(userId = userId.value.toString())
    }
}
