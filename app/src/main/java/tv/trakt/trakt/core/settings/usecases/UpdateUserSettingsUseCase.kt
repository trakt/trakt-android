package tv.trakt.trakt.core.settings.usecases

import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.user.data.remote.UserRemoteDataSource
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId

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

    suspend fun updateMultiplePlays(enabled: Boolean) {
        remoteSource.updateMultiplePlays(enabled)
        remoteSource.getProfile().let {
            sessionManager.saveProfile(it)
        }
    }

    suspend fun updateCoverImage(
        mediaId: TraktId?,
        mediaType: MediaType?,
    ) {
        remoteSource.updateCoverImage(
            mediaId = mediaId,
            mediaType = mediaType,
        )

        remoteSource.getProfile().let {
            sessionManager.saveProfile(it)
        }
    }
}
