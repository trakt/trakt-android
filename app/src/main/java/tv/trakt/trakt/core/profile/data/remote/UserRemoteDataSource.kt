package tv.trakt.trakt.core.profile.data.remote

import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.networking.SocialActivityItemDto

internal interface UserRemoteDataSource {
    suspend fun getProfile(): User

    suspend fun getSocialActivity(
        limit: Int,
        type: String,
    ): List<SocialActivityItemDto>
}
