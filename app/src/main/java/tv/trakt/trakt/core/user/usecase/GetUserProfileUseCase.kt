package tv.trakt.trakt.core.user.usecase

import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource

internal class GetUserProfileUseCase(
    private val remoteSource: UserRemoteDataSource,
    private val sessionManager: SessionManager,
) {
    suspend fun loadUserProfile() {
        if (!sessionManager.isAuthenticated()) {
            return
        }
        val user = remoteSource.getProfile()
        sessionManager.saveProfile(user)
    }
}
