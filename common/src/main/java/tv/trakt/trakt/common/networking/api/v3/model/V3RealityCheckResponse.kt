package tv.trakt.trakt.common.networking.api.v3.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class V3RealityCheckResponse(
    val applicable: Boolean = false,
    val verdict: String? = null,
    val story: String? = null,
    val protagonists: List<V3RealityProtagonist>? = null,
    val summary: String? = null,
    val breakdown: String? = null,
    val claims: List<V3RealityClaim>? = null,
    val characters: List<V3RealityCharacter>? = null,
    val timeline: String? = null,
    val leftOut: List<String>? = null,
) {
    @Immutable
    @Serializable
    data class V3RealityProtagonist(
        val name: String,
        val tier: String,
    )

    @Immutable
    @Serializable
    data class V3RealityClaim(
        val about: String? = null,
        val onScreen: String,
        val documented: String,
        val label: String,
        val impact: String? = null,
    )

    @Immutable
    @Serializable
    data class V3RealityCharacter(
        val name: String,
        val kind: String,
        val note: String? = null,
    )
}
