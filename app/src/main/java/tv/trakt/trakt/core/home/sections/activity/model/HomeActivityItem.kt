package tv.trakt.trakt.core.home.sections.activity.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import java.time.Instant
import kotlin.time.Duration

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

    val key: String
        get() = when (this) {
            is MovieItem -> "${MediaType.MOVIE.value}-${movie.ids.trakt.value}"
            is EpisodeItem -> "${MediaType.EPISODE.value}-${episode.ids.trakt.value}"
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
}
