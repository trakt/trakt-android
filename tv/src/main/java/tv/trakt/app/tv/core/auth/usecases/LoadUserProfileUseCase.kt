package tv.trakt.app.tv.core.auth.usecases

import tv.trakt.app.tv.auth.session.SessionManager
import tv.trakt.app.tv.core.profile.data.remote.ProfileRemoteDataSource

internal class LoadUserProfileUseCase(
    private val remoteSource: ProfileRemoteDataSource,
    private val sessionManager: SessionManager,
) {
    suspend fun loadUserProfile() {
        if (!sessionManager.isAuthenticated()) {
            return
        }
        val user = remoteSource.getUserProfile()
        sessionManager.saveProfile(user)
    }
}
