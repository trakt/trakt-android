package tv.trakt.trakt.core.lists.sections.watchlist.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime

@Immutable
internal sealed class WatchlistItem(
    open val rank: Int,
    open val listedAt: Instant,
) {
    @Immutable
    internal data class MovieItem(
        val movie: Movie,
        override val rank: Int,
        override val listedAt: Instant,
    ) : WatchlistItem(rank, listedAt)

    @Immutable
    internal data class ShowItem(
        val show: Show,
        override val rank: Int,
        override val listedAt: Instant,
    ) : WatchlistItem(rank, listedAt)

    val id: TraktId
        get() = when (this) {
            is ShowItem -> show.ids.trakt
            is MovieItem -> movie.ids.trakt
        }

    val key: String
        get() = when (this) {
            is ShowItem -> "${show.ids.trakt.value}-show"
            is MovieItem -> "${movie.ids.trakt.value}-movie"
        }

    val images: Images?
        get() = when (this) {
            is ShowItem -> show.images
            is MovieItem -> movie.images
        }

    val released: ZonedDateTime?
        get() = when (this) {
            is ShowItem -> show.released
            is MovieItem -> movie.released?.atStartOfDay(UTC)
        }
}
