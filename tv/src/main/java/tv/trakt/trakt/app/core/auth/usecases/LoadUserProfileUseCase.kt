package tv.trakt.trakt.app.core.auth.usecases

import tv.trakt.trakt.app.core.profile.data.remote.ProfileRemoteDataSource
import tv.trakt.trakt.common.auth.session.SessionManager

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
