package tv.trakt.trakt.common.networking.api.v3.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.networking.MovieDto
import tv.trakt.trakt.common.networking.ShowDto

@Immutable
@Serializable
data class V3RecommendationSource(
    val id: Long,
    val type: String,
    val movie: MovieDto? = null,
    val show: ShowDto? = null,
    val subgenres: List<Subgenre>? = null,
) {
    @Immutable
    @Serializable
    data class Subgenre(
        val id: Long,
        val name: String,
        val slug: String,
    )
}
