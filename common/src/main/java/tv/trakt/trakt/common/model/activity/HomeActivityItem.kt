package tv.trakt.trakt.common.model.activity

import androidx.compose.runtime.Immutable
import org.openapitools.client.models.GetUsersHistoryAll200ResponseInner.Type.EPISODE
import org.openapitools.client.models.GetUsersHistoryAll200ResponseInner.Type.MOVIE
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.networking.SyncHistoryItemDto
import java.time.Instant
import kotlin.time.Duration

@Immutable
sealed class HomeActivityItem(
    open val id: Long,
    open val user: User?,
    open val userRating: UserRating?,
    open val activity: String,
    open val activityAt: Instant,
) {
    @Immutable
    data class MovieItem(
        override val id: Long,
        override val user: User?,
        override val userRating: UserRating?,
        override val activity: String,
        override val activityAt: Instant,
        val movie: Movie,
    ) : HomeActivityItem(id, user, userRating, activity, activityAt)

    @Immutable
    data class EpisodeItem(
        override val id: Long,
        override val user: User?,
        override val userRating: UserRating?,
        override val activity: String,
        override val activityAt: Instant,
        val episode: Episode,
        val show: Show,
    ) : HomeActivityItem(id, user, userRating, activity, activityAt)

    val key: String
        get() = when (this) {
            is MovieItem -> "${MediaType.Movie.value}-${movie.ids.trakt.value}"
            is EpisodeItem -> "${MediaType.Episode.value}-${episode.ids.trakt.value}"
        }

    val title: String
        get() = when (this) {
            is MovieItem -> movie.title
            is EpisodeItem -> show.title
        }

    val titleOriginal: String?
        get() = when (this) {
            is MovieItem -> movie.titleOriginal
            is EpisodeItem -> show.titleOriginal
        }

    val images: Images?
        get() = when (this) {
            is MovieItem -> movie.images
            is EpisodeItem -> show.images
        }

    val runtime: Duration?
        get() = when (this) {
            is MovieItem -> movie.runtime
            is EpisodeItem -> episode.runtime
        }

    val sortId: Long?
        get() = when (this) {
            is MovieItem -> movie.released?.toEpochDay()
            is EpisodeItem -> episode.seasonEpisode.id
        }

    companion object {
        fun fromDto(dto: SyncHistoryItemDto): HomeActivityItem {
            return when (dto.type) {
                EPISODE -> EpisodeItem(
                    id = dto.id,
                    user = null,
                    userRating = null,
                    activity = dto.action.value,
                    activityAt = dto.watchedAt.toInstant(),
                    episode = Episode.fromDto(
                        checkNotNull(dto.episode) {
                            "Episode should not be null if type is EPISODE"
                        },
                    ),
                    show = Show.fromDto(
                        checkNotNull(dto.show) {
                            "Show should not be null if type is EPISODE"
                        },
                    ),
                )
                MOVIE -> MovieItem(
                    id = dto.id,
                    user = null,
                    userRating = null,
                    activity = dto.action.value,
                    activityAt = dto.watchedAt.toInstant(),
                    movie = Movie.fromDto(
                        checkNotNull(dto.movie) {
                            "Movie should not be null if type is MOVIE"
                        },
                    ),
                )
            }
        }
    }
}
