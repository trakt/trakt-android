package tv.trakt.trakt.common.core.movies.data.remote.model

import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.networking.MovieCalendarDto

@Serializable
data class TrendingMovieDto(
    val watchers: Int,
    val movie: MovieCalendarDto,
)
