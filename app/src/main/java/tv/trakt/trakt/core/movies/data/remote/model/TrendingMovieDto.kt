package tv.trakt.trakt.core.movies.data.remote.model

import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.networking.MovieCalendarDto

@Serializable
internal data class TrendingMovieDto(
    val watchers: Int,
    val movie: MovieCalendarDto,
)
