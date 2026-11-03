package tv.trakt.trakt.common.networking.api.v3.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class V3RecommendationReasonsResponse(
    val sources: List<V3RecommendationSource>? = null,
    val people: List<V3RecommendationPeopleReason>? = null,
)
