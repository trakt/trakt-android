package tv.trakt.trakt.common.data.dto

import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.networking.MovieDto
import tv.trakt.trakt.common.networking.ShowDto

@Serializable
data class MixtapeMediaDto(
    val movie: MovieDto? = null,
    val show: ShowDto? = null,
)
