package tv.trakt.trakt.app.core.home.sections.startwatching.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.app.core.home.sections.shows.upnext.model.Progress
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime

@Immutable
internal sealed class WatchlistItem(
    open val rank: Int,
    open val listedAt: ZonedDateTime,
) {
    @Immutable
    internal data class MovieItem(
        override val rank: Int,
        override val listedAt: ZonedDateTime,
        val movie: Movie,
    ) : WatchlistItem(rank, listedAt)

    @Immutable
    internal data class ShowItem(
        override val rank: Int,
        override val listedAt: ZonedDateTime,
        val show: Show,
        val progress: Progress? = null,
    ) : WatchlistItem(rank, listedAt)

    val id: TraktId
        get() = when (this) {
            is ShowItem -> show.ids.trakt
            is MovieItem -> movie.ids.trakt
        }

    val key: String
        get() = when (this) {
            is ShowItem -> "${show.ids.trakt.value}-show-watchlist"
            is MovieItem -> "${movie.ids.trakt.value}-movie-watchlist"
        }

    val title: String
        get() = when (this) {
            is ShowItem -> show.title
            is MovieItem -> movie.title
        }

    val posterImage: String?
        get() = when (this) {
            is ShowItem -> show.images?.getPosterUrl(Images.Size.MEDIUM)
            is MovieItem -> movie.images?.getPosterUrl(Images.Size.MEDIUM)
        }

    val fullFanartImage: String?
        get() = when (this) {
            is ShowItem -> show.images?.getFanartUrl(Images.Size.FULL)
            is MovieItem -> movie.images?.getFanartUrl(Images.Size.FULL)
        }

    val released: ZonedDateTime?
        get() = when (this) {
            is ShowItem -> show.released
            is MovieItem -> movie.released?.atStartOfDay(UTC)
        }
}
