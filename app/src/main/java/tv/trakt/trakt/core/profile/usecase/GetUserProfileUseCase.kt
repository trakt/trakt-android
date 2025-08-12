package tv.trakt.trakt.core.profile.usecase

import tv.trakt.trakt.common.auth.session.SessionManager

internal class GetUserProfileUseCase(
//    private val remoteSource: ProfileRemoteDataSource,
    private val sessionManager: SessionManager,
) {
    suspend fun loadUserProfile() {
        if (!sessionManager.isAuthenticated()) {
            return
        }
//        val user = remoteSource.getUserProfile()
//        sessionManager.saveProfile(user)
    }
}
