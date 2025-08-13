package tv.trakt.trakt.core.profile.usecase

import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.core.profile.data.remote.UserRemoteDataSource

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
