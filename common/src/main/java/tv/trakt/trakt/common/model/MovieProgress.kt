package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.Movie.Companion
import tv.trakt.trakt.common.networking.MovieProgressDto

@Immutable
@Serializable
data class MovieProgress(
    val ids: Ids,
    val title: String,
    val year: Int?,
) {
    companion object
}

fun Companion.fromDto(dto: MovieProgressDto): MovieProgress {
    return MovieProgress(
        ids = Ids.fromDto(dto.ids),
        title = dto.title,
        year = dto.year,
    )
}
