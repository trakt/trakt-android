package tv.trakt.trakt.app.core.movies.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class AnticipatedMovie(
    val listCount: Int,
    val movie: Movie,
)
