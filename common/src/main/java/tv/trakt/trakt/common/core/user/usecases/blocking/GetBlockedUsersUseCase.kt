package tv.trakt.trakt.common.core.user.usecases.blocking

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.user.data.remote.social.UserSocialRemoteDataSource
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.fromDto
import java.time.Instant

class GetBlockedUsersUseCase(
    private val sessionManager: SessionManager,
    private val remoteSource: UserSocialRemoteDataSource,
) {
    suspend fun getBlockedUsers(): ImmutableMap<User, Instant> {
        if (!sessionManager.isAuthenticated()) {
            return emptyMap<User, Instant>().toImmutableMap()
        }
        return remoteSource.getBlockedUsers()
            .mapKeys { User.fromDto(it.key) }
            .toImmutableMap()
    }
}
