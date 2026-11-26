package tv.trakt.trakt.common.networking.api.v3.model

import kotlinx.serialization.Serializable

@Serializable
data class V3NudgeResponse(
    val nudge: V3Nudge? = null,
) {
    @Serializable
    data class V3Nudge(
        val type: String,
        val limit: String? = null,
        val used: Int? = null,
        val total: Int? = null,
    )
}
