package tv.trakt.trakt.core.user.usecases.social

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.user.data.remote.social.UserSocialRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.user.model.UserFollowRequest

internal class LoadUserSocialRequestsUseCase(
    private val remoteSource: UserSocialRemoteDataSource,
) {
    suspend fun loadRequests(): ImmutableList<UserFollowRequest> {
        val response = remoteSource.getRequests(
            extended = "full,images,vip",
        ).asyncMap {
            UserFollowRequest(
                id = it.id,
                requestedAt = it.requestedAt.toInstant(),
                user = User.fromDto(it.user),
            )
        }
        return response
            .toImmutableList()
    }
}
