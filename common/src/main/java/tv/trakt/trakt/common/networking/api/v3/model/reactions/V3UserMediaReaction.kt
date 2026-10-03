package tv.trakt.trakt.common.networking.api.v3.model.reactions

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class V3UserMediaReaction(
    val id: Long,
    val reaction: V3MediaReaction,
)
