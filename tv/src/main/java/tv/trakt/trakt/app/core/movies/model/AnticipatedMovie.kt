package tv.trakt.trakt.app.core.movies.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Movie

@Immutable
internal data class AnticipatedMovie(
    val listCount: Int,
    val movie: Movie,
)
