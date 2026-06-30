package tv.trakt.trakt.common.core.user.usecases.following

import tv.trakt.trakt.common.core.user.data.remote.social.UserSocialRemoteDataSource

class FollowRequestUserUseCase(
    private val remoteSource: UserSocialRemoteDataSource,
) {
    suspend fun approveRequest(requestId: Int) {
        remoteSource.approveRequest(requestId)
    }

    suspend fun rejectRequest(requestId: Int) {
        remoteSource.rejectRequest(requestId)
    }
}
