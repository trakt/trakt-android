package tv.trakt.trakt.common.networking.api.v3.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.networking.MovieDto
import tv.trakt.trakt.common.networking.ShowDto

@Immutable
@Serializable
data class V3MediaRecommendationResponse(
    val show: ShowDto? = null,
    val movie: MovieDto? = null,
    val sources: List<V3RecommendationSource>? = null,
)
