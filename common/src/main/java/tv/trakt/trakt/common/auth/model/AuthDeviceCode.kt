package tv.trakt.trakt.common.auth.model

import java.time.ZonedDateTime
import kotlin.time.Duration

data class AuthDeviceCode(
    val url: String,
    val userCode: String,
    val deviceCode: String,
    val expiresIn: Duration,
    val expiresAt: ZonedDateTime,
    val interval: Duration,
)
