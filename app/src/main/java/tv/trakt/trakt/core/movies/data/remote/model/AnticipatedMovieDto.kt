package tv.trakt.trakt.core.movies.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.networking.MovieDto

@Serializable
internal data class AnticipatedMovieDto(
    @SerialName("list_count") val listCount: Int,
    val movie: MovieDto,
)
