package tv.trakt.trakt.app.core.movies.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Movie

@Immutable
internal data class TrendingMovie(
    val watchers: Int,
    val movie: Movie,
)
