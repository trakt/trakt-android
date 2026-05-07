package tv.trakt.trakt.core.user.usecases

import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.user.data.remote.UserRemoteDataSource
import tv.trakt.trakt.common.model.User

internal class LoadUserProfileUseCase(
    private val remoteSource: UserRemoteDataSource,
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
