package tv.trakt.trakt.tv.core.movies.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tv.trakt.trakt.tv.networking.openapi.MovieDto

@Serializable
internal data class AnticipatedMovieDto(
    @SerialName("list_count") val listCount: Int,
    val movie: MovieDto,
)
