package tv.trakt.trakt.common.model.profile

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.ratings.UserRating
import java.time.Instant

@Immutable
sealed interface ProfileRatingItem {
    val rating: Int
    val ratedAt: Instant

    @Immutable
    data class ShowItem(
        val show: Show,
        override val rating: Int,
        override val ratedAt: Instant,
    ) : ProfileRatingItem

    @Immutable
    data class MovieItem(
        val movie: Movie,
        override val rating: Int,
        override val ratedAt: Instant,
    ) : ProfileRatingItem

    @Immutable
    data class EpisodeItem(
        val show: Show,
        val episode: Episode,
        override val rating: Int,
        override val ratedAt: Instant,
    ) : ProfileRatingItem

    @Immutable
    data class SeasonItem(
        val show: Show,
        val season: Season,
        override val rating: Int,
        override val ratedAt: Instant,
    ) : ProfileRatingItem

    val key: String
        get() = when (this) {
            is ShowItem -> "show-${show.ids.trakt.value}"
            is MovieItem -> "movie-${movie.ids.trakt.value}"
            is EpisodeItem -> "episode-${show.ids.trakt.value}-${episode.ids.trakt.value}"
            is SeasonItem -> "season-${show.ids.trakt.value}-${season.ids.trakt.value}"
        }

    val userRating: UserRating
        get() {
            val (mediaId, mediaType) = when (this) {
                is ShowItem -> show.ids.trakt to MediaType.Show
                is MovieItem -> movie.ids.trakt to MediaType.Movie
                is EpisodeItem -> episode.ids.trakt to MediaType.Episode
                is SeasonItem -> season.ids.trakt to MediaType.Season
            }
            return UserRating(
                mediaId = mediaId,
                mediaType = mediaType,
                rating = rating,
            )
        }

    fun withRating(rating: Int): ProfileRatingItem {
        return when (this) {
            is ShowItem -> copy(rating = rating)
            is MovieItem -> copy(rating = rating)
            is EpisodeItem -> copy(rating = rating)
            is SeasonItem -> copy(rating = rating)
        }
    }
}
