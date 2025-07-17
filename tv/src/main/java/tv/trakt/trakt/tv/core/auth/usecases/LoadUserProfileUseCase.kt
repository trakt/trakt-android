package tv.trakt.trakt.tv.core.auth.usecases

import tv.trakt.trakt.tv.auth.session.SessionManager
import tv.trakt.trakt.tv.core.profile.data.remote.ProfileRemoteDataSource

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
