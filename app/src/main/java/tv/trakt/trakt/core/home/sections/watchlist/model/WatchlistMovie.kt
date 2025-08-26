package tv.trakt.trakt.core.home.sections.watchlist.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Movie
import java.time.Instant

@Immutable
internal data class WatchlistMovie(
    val movie: Movie,
    val listedAt: Instant,
    val rank: Int?,
)
