package tv.trakt.trakt.app.core.auth.usecases

import tv.trakt.trakt.app.core.profile.data.remote.ProfileRemoteDataSource
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.model.User

internal class LoadUserProfileUseCase(
    private val remoteSource: ProfileRemoteDataSource,
    private val sessionManager: SessionManager,
) {
    suspend fun loadUserProfile(): User? {
        if (!sessionManager.isAuthenticated()) {
            return null
        }
        val user = remoteSource.getUserProfile()
        sessionManager.saveProfile(user)
        return user
    }
}
