package tv.trakt.trakt.common.networking.api.v3.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class V3PushTokenRequest(
    val token: String,
    @SerialName("device_id")
    val deviceId: String,
    val platform: String,
)
