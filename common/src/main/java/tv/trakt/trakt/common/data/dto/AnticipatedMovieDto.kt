package tv.trakt.trakt.common.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.networking.MovieCalendarDto

@Serializable
data class AnticipatedMovieDto(
    @SerialName("list_count") val listCount: Int,
    val movie: MovieCalendarDto,
)
