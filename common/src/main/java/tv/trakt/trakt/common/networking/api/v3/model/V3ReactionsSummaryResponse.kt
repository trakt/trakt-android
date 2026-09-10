package tv.trakt.trakt.common.networking.api.v3.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class V3ReactionsSummaryResponse(
    val total: Int? = null,
    val reactions: List<V3ReactionCount>? = null,
) {
    @Immutable
    @Serializable
    data class V3ReactionCount(
        val id: Long? = null,
        @SerialName("reaction_type")
        val reactionType: String,
        val count: Int,
    )
}
