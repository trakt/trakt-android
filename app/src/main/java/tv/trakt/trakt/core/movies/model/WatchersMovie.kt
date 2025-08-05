package tv.trakt.trakt.core.movies.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Movie

@Immutable
internal data class WatchersMovie(
    val watchers: Int,
    val movie: Movie,
)
