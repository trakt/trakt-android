package tv.trakt.app.tv.core.home.sections.movies.availablenow.model

import androidx.compose.runtime.Immutable
import tv.trakt.app.tv.core.movies.model.Movie
import java.time.ZonedDateTime

@Immutable
internal data class WatchlistMovie(
    val movie: Movie,
    val listedAt: ZonedDateTime,
    val rank: Int?,
)
