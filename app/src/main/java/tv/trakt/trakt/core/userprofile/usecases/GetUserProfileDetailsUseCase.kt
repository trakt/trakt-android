package tv.trakt.trakt.core.userprofile.usecases

import tv.trakt.trakt.common.core.user.data.remote.UserRemoteSource
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User

internal class GetUserProfileDetailsUseCase(
    private val remoteSource: UserRemoteSource,
) {
    suspend fun getUserProfileDetails(id: TraktId): User {
        return remoteSource.getUserProfile(id)
    }
}
