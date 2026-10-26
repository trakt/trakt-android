package tv.trakt.trakt.common.networking.api.v3.model

import kotlinx.serialization.Serializable

@Serializable
data class V3YouTubeSpecialResponse(
    val videoId: String,
    val title: String,
    val channel: String,
    val durationSeconds: Int? = null,
    val source: String? = null,
)
