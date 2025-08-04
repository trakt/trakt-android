package tv.trakt.trakt.app.core.movies.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class TrendingMovie(
    val watchers: Int,
    val movie: Movie,
)
