package tv.trakt.trakt.app.core.movies.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.networking.MovieCalendarDto

@Serializable
internal data class AnticipatedMovieDto(
    @SerialName("list_count") val listCount: Int,
    val movie: MovieCalendarDto,
)
