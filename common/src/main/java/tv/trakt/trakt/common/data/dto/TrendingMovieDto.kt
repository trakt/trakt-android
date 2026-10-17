package tv.trakt.trakt.common.data.dto

import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.networking.MovieCalendarDto

@Serializable
data class TrendingMovieDto(
    val watchers: Int,
    val movie: MovieCalendarDto,
)
