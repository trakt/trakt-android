package tv.trakt.trakt.tv.core.movies.data.remote.model.response

import kotlinx.serialization.Serializable
import tv.trakt.trakt.tv.networking.openapi.MovieDto

@Serializable
internal data class TrendingMovieDto(
    val watchers: Int,
    val movie: MovieDto,
)
