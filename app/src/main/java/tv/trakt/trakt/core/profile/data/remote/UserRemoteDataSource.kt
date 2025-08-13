package tv.trakt.trakt.core.profile.data.remote

import tv.trakt.trakt.common.model.User

internal interface UserRemoteDataSource {
    suspend fun getProfile(): User
}
