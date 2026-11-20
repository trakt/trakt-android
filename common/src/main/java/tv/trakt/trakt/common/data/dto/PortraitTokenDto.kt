package tv.trakt.trakt.common.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PortraitTokenDto(
    val token: String,
    @SerialName("expires_at") val expiresAt: String,
)
