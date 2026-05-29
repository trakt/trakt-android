package tv.trakt.trakt.core.userprofile.sections.social.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.user.data.remote.social.UserSocialRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.fromDto

internal class GetUserProfileSocialUseCase(
    private val remoteSource: UserSocialRemoteDataSource,
) {
    suspend fun getFollowing(userId: TraktId): ImmutableList<User> {
        return remoteSource.getFollowing(
            userId = userId.value.toString(),
            extended = "full,images,vip",
        )
            .entries
            .asyncMap { User.fromDto(it.key) }
            .toImmutableList()
    }

    suspend fun getFollowers(userId: TraktId): ImmutableList<User> {
        return remoteSource.getFollowers(
            userId = userId.value.toString(),
            extended = "full,images,vip",
        )
            .entries
            .asyncMap { User.fromDto(it.key) }
            .toImmutableList()
    }
}
