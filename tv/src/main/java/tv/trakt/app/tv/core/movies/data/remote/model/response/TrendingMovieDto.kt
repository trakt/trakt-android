package tv.trakt.app.tv.core.movies.data.remote.model.response

import kotlinx.serialization.Serializable
import tv.trakt.app.tv.networking.openapi.MovieDto

@Serializable
internal data class TrendingMovieDto(
    val watchers: Int,
    val movie: MovieDto,
)
