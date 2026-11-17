package tv.trakt.trakt.common.networking.api.v3.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class V3DocumentaryCheckResponse(
    val applicable: Boolean = false,
    val tier: String? = null,
    val summary: String? = null,
    val makers: V3DocumentaryMakers? = null,
    val disputes: List<V3DocumentaryDispute>? = null,
) {
    @Immutable
    @Serializable
    data class V3DocumentaryMakers(
        val madeBy: List<String> = emptyList(),
        val fundedBy: List<String> = emptyList(),
    )

    @Immutable
    @Serializable
    data class V3DocumentaryDispute(
        val claim: String,
        val by: String,
        val type: String,
        val year: Int? = null,
        val reply: String? = null,
        val impact: String? = null,
    )
}
