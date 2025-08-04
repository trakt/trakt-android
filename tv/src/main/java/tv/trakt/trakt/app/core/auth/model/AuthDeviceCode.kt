package tv.trakt.trakt.app.core.auth.model

import java.time.ZonedDateTime
import kotlin.time.Duration

internal data class AuthDeviceCode(
    val url: String,
    val userCode: String,
    val deviceCode: String,
    val expiresIn: Duration,
    val expiresAt: ZonedDateTime,
    val interval: Duration,
)
