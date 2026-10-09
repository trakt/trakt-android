package tv.trakt.trakt.common.networking.api.v3.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class V3ShareClickResponse(
    @SerialName("outcome")
    val result: String,
)
