package tv.trakt.trakt.core.home.sections.activity.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.episodes.model.Episode
import java.time.Instant

@Immutable
internal sealed class HomeActivityItem(
    open val id: Long,
    open val user: User?,
    open val activity: String,
    open val activityAt: Instant,
) {
    @Immutable
    internal data class MovieItem(
        override val id: Long,
        override val user: User?,
        override val activity: String,
        override val activityAt: Instant,
        val movie: Movie,
    ) : HomeActivityItem(id, user, activity, activityAt)

    @Immutable
    internal data class EpisodeItem(
        override val id: Long,
        override val user: User?,
        override val activity: String,
        override val activityAt: Instant,
        val episode: Episode,
        val show: Show,
    ) : HomeActivityItem(id, user, activity, activityAt)

    val images: Images?
        get() = when (this) {
            is MovieItem -> movie.images
            is EpisodeItem -> show.images
        }
}
