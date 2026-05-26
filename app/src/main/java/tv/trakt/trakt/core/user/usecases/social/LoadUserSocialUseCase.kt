package tv.trakt.trakt.core.user.usecases.social

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import tv.trakt.trakt.common.core.user.data.remote.social.UserSocialRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.toTraktId

/**
 * Loads the user's socials from the remote source and updates the local cache.
 */
internal class LoadUserSocialUseCase(
    private val remoteSource: UserSocialRemoteDataSource,
) {
    suspend fun loadFollowingIds(): ImmutableSet<TraktId> {
        return remoteSource.getFollowing(
            userId = "me",
            extended = null,
        )
            .entries
            .asyncMap { it.key.ids.trakt.toTraktId() }
            .toImmutableSet()
    }

    suspend fun loadFollowing(): ImmutableList<User> {
        val response = remoteSource.getFollowing(
            userId = "me",
            extended = "full,images,vip",
        ).entries
            .asyncMap {
                User.fromDto(it.key)
            }

        return response.toImmutableList()
    }

    suspend fun loadFollowers(): ImmutableList<User> {
        val response = remoteSource.getFollowers(
            userId = "me",
            extended = "full,images,vip",
        )
            .entries
            .asyncMap {
                User.fromDto(it.key)
            }

        return response.toImmutableList()
    }
}
