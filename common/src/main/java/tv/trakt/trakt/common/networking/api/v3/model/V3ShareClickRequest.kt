package tv.trakt.trakt.common.networking.api.v3.model

import kotlinx.serialization.Serializable

@Serializable
data class V3ShareClickRequest(
    val code: String,
    val url: String,
)
