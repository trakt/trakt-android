package tv.trakt.trakt.core.user.usecases.social

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource

/**
 * Loads the user's socials from the remote source and updates the local cache.
 */
internal class LoadUserSocialUseCase(
    private val remoteSource: UserRemoteDataSource,
) {
    suspend fun loadFollowing(): ImmutableList<User> {
        val response = remoteSource.getFollowing()
            .entries
            .asyncMap {
                User.fromDto(it.key)
            }

        return response.toImmutableList()
    }

    suspend fun loadFollowers(): ImmutableList<User> {
        val response = remoteSource.getFollowers()
            .entries
            .asyncMap {
                User.fromDto(it.key)
            }

        return response.toImmutableList()
    }
}
