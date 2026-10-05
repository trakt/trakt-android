package tv.trakt.trakt.common.networking.api.v3.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class V3ParentalGuideResponse(
    val guide: List<V3ParentalGuideEntry>,
) {
    @Immutable
    @Serializable
    data class V3ParentalGuideEntry(
        val category: String,
        val severity: String,
    )
}
