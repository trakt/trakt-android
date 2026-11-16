package tv.trakt.trakt.app.core.details.lists.details.media.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.app.core.lists.model.ListSortValues
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.networking.ListItemDto
import java.time.Instant

@Immutable
internal sealed interface ListMediaItem {
    val id: TraktId
    val title: String
    val images: Images?
    val listedAt: Instant?
    val userRating: UserRating?

    data class ShowItem(
        val show: Show,
        override val listedAt: Instant? = null,
        override val userRating: UserRating? = null,
    ) : ListMediaItem {
        override val id = show.ids.trakt
        override val title = show.title
        override val images = show.images
    }

    data class MovieItem(
        val movie: Movie,
        override val listedAt: Instant? = null,
        override val userRating: UserRating? = null,
    ) : ListMediaItem {
        override val id = movie.ids.trakt
        override val title = movie.title
        override val images = movie.images
    }

    data class SeasonItem(
        val show: Show,
        val season: Season,
        override val listedAt: Instant? = null,
    ) : ListMediaItem {
        override val id = season.ids.trakt
        override val title = show.title
        override val images = season.images.takeIf { !it?.poster.isNullOrEmpty() } ?: show.images
        override val userRating: UserRating? = null
    }

    data class EpisodeItem(
        val show: Show,
        val episode: Episode,
        override val listedAt: Instant? = null,
    ) : ListMediaItem {
        override val id = episode.ids.trakt
        override val title = show.title
        override val images = show.images
        override val userRating: UserRating? = null
    }

    val sortValues: ListSortValues
        get() = when (this) {
            is ShowItem -> ListSortValues.of(show, listedAt, userRating)
            is MovieItem -> ListSortValues.of(movie, listedAt, userRating)
            is SeasonItem -> ListSortValues.of(season, listedAt)
            is EpisodeItem -> ListSortValues.of(episode, listedAt)
        }

    val key: String
        get() = when (this) {
            is ShowItem -> "show-${id.value}-tv"
            is MovieItem -> "movie-${id.value}-tv"
            is SeasonItem -> "season-${id.value}-tv"
            is EpisodeItem -> "episode-${id.value}-tv"
        }

    companion object {
        fun fromDto(
            dto: ListItemDto,
            showRatings: Map<TraktId, UserRating>,
            movieRatings: Map<TraktId, UserRating>,
        ): ListMediaItem? {
            val listedAt = dto.listedAt.toInstant()
            val show = dto.show?.let { Show.fromDto(it) }

            return when (dto.type.value) {
                MediaType.Show.value -> {
                    show?.let {
                        ShowItem(
                            show = it,
                            listedAt = listedAt,
                            userRating = showRatings[it.ids.trakt],
                        )
                    }
                }

                MediaType.Movie.value -> {
                    dto.movie?.let { Movie.fromDto(it) }?.let {
                        MovieItem(
                            movie = it,
                            listedAt = listedAt,
                            userRating = movieRatings[it.ids.trakt],
                        )
                    }
                }

                MediaType.Season.value -> {
                    val season = dto.season?.let { Season.fromDto(it) } ?: return null
                    SeasonItem(
                        show = show ?: return null,
                        season = season,
                        listedAt = listedAt,
                    )
                }

                MediaType.Episode.value -> {
                    val episode = dto.episode?.let { Episode.fromDto(it) } ?: return null
                    EpisodeItem(
                        show = show ?: return null,
                        episode = episode,
                        listedAt = listedAt,
                    )
                }

                else -> {
                    null
                }
            }
        }
    }
}
