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
import tv.trakt.trakt.common.model.ratings.UserRating
import java.time.Instant
import java.time.ZoneOffset.UTC
import kotlin.time.Duration

@Immutable
internal sealed class CustomListItem(
    open val itemId: Int,
    open val rank: Int,
    open val listedAt: Instant,
    open val userRating: UserRating?,
    open val loading: Boolean,
) {
    @Immutable
    internal data class MovieItem(
        val movie: Movie,
        override val itemId: Int,
        override val rank: Int,
        override val listedAt: Instant,
        override val userRating: UserRating? = null,
        override val loading: Boolean = false,
    ) : CustomListItem(itemId, rank, listedAt, userRating, loading)

    @Immutable
    internal data class ShowItem(
        val show: Show,
        override val itemId: Int,
        override val rank: Int,
        override val listedAt: Instant,
        override val userRating: UserRating? = null,
        override val loading: Boolean = false,
    ) : CustomListItem(itemId, rank, listedAt, userRating, loading)

    @Immutable
    internal data class SeasonItem(
        val show: Show,
        val season: Season,
        override val itemId: Int,
        override val rank: Int,
        override val listedAt: Instant,
        override val userRating: UserRating? = null,
        override val loading: Boolean = false,
    ) : CustomListItem(itemId, rank, listedAt, userRating, loading)

    @Immutable
    internal data class EpisodeItem(
        val show: Show,
        val episode: Episode,
        override val itemId: Int,
        override val rank: Int,
        override val listedAt: Instant,
        override val userRating: UserRating? = null,
        override val loading: Boolean = false,
    ) : CustomListItem(itemId, rank, listedAt, userRating, loading)

    val id: TraktId
        get() = when (this) {
            is ShowItem -> show.ids.trakt
            is MovieItem -> movie.ids.trakt
            is SeasonItem -> season.ids.trakt
            is EpisodeItem -> episode.ids.trakt
        }

    val type: MediaType
        get() = when (this) {
            is ShowItem -> MediaType.Show
            is MovieItem -> MediaType.Movie
            is SeasonItem -> MediaType.Season
            is EpisodeItem -> MediaType.Episode
        }

    val key: String
        get() = "${id.value}-${type.value}-cli"

    val title: String
        get() = when (this) {
            is ShowItem -> show.title
            is MovieItem -> movie.title
            is SeasonItem -> show.title
            is EpisodeItem -> show.title
        }

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
