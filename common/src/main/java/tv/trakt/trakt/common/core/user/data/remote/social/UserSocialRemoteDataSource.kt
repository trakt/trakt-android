package tv.trakt.trakt.common.core.user.data.remote.social

import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.networking.SocialActivityItemDto
import tv.trakt.trakt.common.networking.UserCommentsDto
import java.time.ZonedDateTime

interface UserSocialRemoteDataSource {
    suspend fun getSocialActivity(
        page: Int? = null,
        limit: Int,
        type: String,
        filters: GlobalFilter?,
    ): List<SocialActivityItemDto>

    suspend fun getFollowing(): Map<UserCommentsDto, ZonedDateTime>

    suspend fun getFollowers(): Map<UserCommentsDto, ZonedDateTime>
}
