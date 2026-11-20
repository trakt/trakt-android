package tv.trakt.trakt.common.model

import java.time.Instant

data class PortraitToken(
    val token: String,
    val expiresAt: Instant,
)
