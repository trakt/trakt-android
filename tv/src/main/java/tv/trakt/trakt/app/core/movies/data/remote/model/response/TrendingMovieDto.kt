package tv.trakt.trakt.app.core.movies.data.remote.model.response

import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.networking.MovieCalendarDto

@Serializable
internal data class TrendingMovieDto(
    val watchers: Int,
    val movie: MovieCalendarDto,
)
