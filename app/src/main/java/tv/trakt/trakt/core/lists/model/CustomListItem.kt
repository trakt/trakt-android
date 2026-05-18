package tv.trakt.trakt.core.lists.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Rating
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant
import java.time.ZoneOffset.UTC
import kotlin.time.Duration

@Immutable
internal sealed class CustomListItem(
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
    ) : CustomListItem(rank, listedAt, loading)

    @Immutable
    internal data class ShowItem(
        val show: Show,
        override val rank: Int,
        override val listedAt: Instant,
        override val loading: Boolean = false,
    ) : CustomListItem(rank, listedAt, loading)

    @Immutable
    internal data class SeasonItem(
        val show: Show,
        val season: Season,
        override val rank: Int,
        override val listedAt: Instant,
        override val loading: Boolean = false,
    ) : CustomListItem(rank, listedAt, loading)

    @Immutable
    internal data class EpisodeItem(
        val show: Show,
        val episode: Episode,
        override val rank: Int,
        override val listedAt: Instant,
        override val loading: Boolean = false,
    ) : CustomListItem(rank, listedAt, loading)

    val id: TraktId
        get() = when (this) {
            is ShowItem -> show.ids.trakt
            is MovieItem -> movie.ids.trakt
            is SeasonItem -> season.ids.trakt
            is EpisodeItem -> episode.ids.trakt
        }

    val type: MediaType
        get() = when (this) {
            is ShowItem -> MediaType.SHOW
            is MovieItem -> MediaType.MOVIE
            is SeasonItem -> MediaType.SEASON
            is EpisodeItem -> MediaType.EPISODE
        }

    val key: String
        get() = "${id.value}-${type.value}-cli"

    val images: Images?
        get() = when (this) {
            is ShowItem -> show.images
            is MovieItem -> movie.images
            is EpisodeItem -> show.images
            is SeasonItem -> season.images.takeIf {
                !it?.poster.isNullOrEmpty()
            } ?: show.images
        }

    val rating: Rating
        get() = when (this) {
            is ShowItem -> show.rating
            is MovieItem -> movie.rating
            is SeasonItem -> season.rating
            is EpisodeItem -> episode.rating
        }

    val runtime: Duration?
        get() = when (this) {
            is ShowItem -> show.runtime
            is MovieItem -> movie.runtime
            is SeasonItem -> Duration.ZERO
            is EpisodeItem -> episode.runtime
        }

    val released: Instant?
        get() = when (this) {
            is ShowItem -> show.releasedAt
            is MovieItem -> movie.released?.atStartOfDay(UTC)?.toInstant()
            is SeasonItem -> season.firstAired?.toInstant()
            is EpisodeItem -> episode.releasedAt
        }

    val airedEpisodes: Int?
        get() = when (this) {
            is ShowItem -> show.airedEpisodes
            else -> null
        }
}
