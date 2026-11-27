package tv.trakt.trakt.common.data.dto

import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.networking.MovieDto
import tv.trakt.trakt.common.networking.ShowDto

@Serializable
data class MixtapeDto(
    val fits: List<Fit> = emptyList(),
    val suggestions: List<Suggestion> = emptyList(),
) {
    @Serializable
    data class Fit(
        val id: Int,
        val score: Double,
        val movie: MovieDto? = null,
        val show: ShowDto? = null,
    )

    @Serializable
    data class Suggestion(
        val because: Int,
        val match: Double,
        val movie: MovieDto? = null,
        val show: ShowDto? = null,
    )
}
