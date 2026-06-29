package tv.trakt.trakt.core.favorites.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Rating
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.ratings.UserRating
import java.time.Instant
import java.time.ZoneOffset.UTC
import kotlin.time.Duration

@Immutable
internal sealed class FavoriteItem(
    open val rank: Int,
    open val listedAt: Instant,
    open val userRating: UserRating?,
    open val loading: Boolean,
) {
    @Immutable
    internal data class MovieItem(
        val movie: Movie,
        override val rank: Int,
        override val listedAt: Instant,
        override val loading: Boolean = false,
        override val userRating: UserRating? = null,
    ) : FavoriteItem(rank, listedAt, userRating, loading)

    @Immutable
    internal data class ShowItem(
        val show: Show,
        override val rank: Int,
        override val listedAt: Instant,
        override val loading: Boolean = false,
        override val userRating: UserRating? = null,
    ) : FavoriteItem(rank, listedAt, userRating, loading)

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
            is ShowItem -> MediaType.Show
            is MovieItem -> MediaType.Movie
        }

    val images: Images?
        get() = when (this) {
            is ShowItem -> show.images
            is MovieItem -> movie.images
        }

    val rating: Rating
        get() = when (this) {
            is ShowItem -> show.rating
            is MovieItem -> movie.rating
        }

    val runtime: Duration?
        get() = when (this) {
            is ShowItem -> show.totalRuntime
            is MovieItem -> movie.runtime
        }

    val released: Instant?
        get() = when (this) {
            is ShowItem -> show.releasedAt
            is MovieItem -> movie.released?.atStartOfDay(UTC)?.toInstant()
        }

    val airedEpisodes: Int?
        get() = when (this) {
            is ShowItem -> show.airedEpisodes
            is MovieItem -> null
        }
}
