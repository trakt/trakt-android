package tv.trakt.app.tv.core.movies.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class TrendingMovie(
    val watchers: Int,
    val movie: Movie,
)
