package tv.trakt.trakt.core.settings.usecases

import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource

internal class UpdateUserSettingsUseCase(
    private val remoteSource: UserRemoteDataSource,
    private val sessionManager: SessionManager,
) {
    suspend fun updateDisplayName(displayName: String?) {
        remoteSource.updateProfileDisplayName(displayName)
        remoteSource.getProfile().let {
            sessionManager.saveProfile(it)
        }
    }

    suspend fun updateLocation(location: String?) {
        remoteSource.updateProfileLocation(location)
        remoteSource.getProfile().let {
            sessionManager.saveProfile(it)
        }
    }

    suspend fun updateAbout(about: String?) {
        remoteSource.updateProfileAbout(about)
        remoteSource.getProfile().let {
            sessionManager.saveProfile(it)
        }
    }
}
