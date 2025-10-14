package tv.trakt.trakt.app.core.home.sections.social.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import java.time.ZonedDateTime

@Immutable
internal sealed class SocialActivityItem(
    open val id: Long,
    open val user: User,
    open val activity: String,
    open val activityAt: ZonedDateTime,
) {
    @Immutable
    internal data class MovieItem(
        override val id: Long,
        override val user: User,
        override val activity: String,
        override val activityAt: ZonedDateTime,
        val movie: Movie,
    ) : SocialActivityItem(id, user, activity, activityAt)

    @Immutable
    internal data class EpisodeItem(
        override val id: Long,
        override val user: User,
        override val activity: String,
        override val activityAt: ZonedDateTime,
        val episode: Episode,
        val show: Show,
    ) : SocialActivityItem(id, user, activity, activityAt)

    val images: Images?
        get() = when (this) {
            is MovieItem -> movie.images
            is EpisodeItem -> show.images
        }

    val sortId: Long?
        get() = when (this) {
            is MovieItem -> movie.released?.toEpochDay()
            is EpisodeItem -> episode.seasonEpisode.id
        }

    val type: String
        get() = when (this) {
            is MovieItem -> "movie"
            is EpisodeItem -> "episode"
        }
}
