package tv.trakt.trakt.core.user.model

import tv.trakt.trakt.common.model.User
import java.time.Instant

internal data class UserFollowRequest(
    val id: Int,
    val requestedAt: Instant,
    val user: User,
)
