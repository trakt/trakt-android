package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.Movie.Companion
import tv.trakt.trakt.common.networking.MovieDto

@Immutable
@Serializable
data class MovieProgress(
    val ids: Ids,
) {
    companion object
}

fun Companion.fromXDto(dto: MovieDto): MovieProgress {
    return MovieProgress(
        ids = Ids.fromDto(dto.ids),
    )
}
