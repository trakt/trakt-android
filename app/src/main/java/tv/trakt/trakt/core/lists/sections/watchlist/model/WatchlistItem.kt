package tv.trakt.trakt.core.lists.sections.watchlist.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.upnext.model.Progress
import java.time.Instant
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime

@Immutable
internal sealed class WatchlistItem(
    open val rank: Int,
    open val listedAt: Instant,
    open val loading: Boolean,
) {
    @Immutable
    internal data class MovieItem(
        val movie: Movie,
        override val rank: Int,
        override val listedAt: Instant,
        override val loading: Boolean = false,
    ) : WatchlistItem(rank, listedAt, loading)

    @Immutable
    internal data class ShowItem(
        val show: Show,
        val progress: Progress? = null,
        override val rank: Int,
        override val listedAt: Instant,
        override val loading: Boolean = false,
    ) : WatchlistItem(rank, listedAt, loading)

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

    val title: String
        get() = when (this) {
            is ShowItem -> show.title
            is MovieItem -> movie.title
        }

    val type: MediaType
        get() = when (this) {
            is ShowItem -> MediaType.SHOW
            is MovieItem -> MediaType.MOVIE
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
