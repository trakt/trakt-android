package tv.trakt.trakt.common.core.user.usecases

import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.data.remote.UserRemoteSource
import tv.trakt.trakt.common.model.User

class LoadUserProfileUseCase(
    private val remoteSource: UserRemoteSource,
    private val sessionManager: SessionManager,
) {
    suspend fun loadUserProfile(): User? {
        if (!sessionManager.isAuthenticated()) {
            return null
        }
        val user = remoteSource.getProfile()
        sessionManager.saveProfile(user)
        return user
    }
}
