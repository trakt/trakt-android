package tv.trakt.trakt.common.core.user.usecases.following

import tv.trakt.trakt.common.data.remote.UserSocialRemoteSource

class FollowRequestUserUseCase(
    private val remoteSource: UserSocialRemoteSource,
) {
    suspend fun approveRequest(requestId: Int) {
        remoteSource.approveRequest(requestId)
    }

    suspend fun rejectRequest(requestId: Int) {
        remoteSource.rejectRequest(requestId)
    }
}
