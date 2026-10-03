package tv.trakt.trakt.common.networking.api.v3.model.reactions

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class V3MediaReactionsSummaryResponse(
    @SerialName("reaction_count")
    val reactionCount: Int,
    @SerialName("user_count")
    val userCount: Int,
    val distribution: Map<String, Int>,
)
