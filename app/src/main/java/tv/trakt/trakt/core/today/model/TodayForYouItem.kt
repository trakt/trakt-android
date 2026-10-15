package tv.trakt.trakt.core.today.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import java.time.Instant
import java.time.ZoneId

@Immutable
internal sealed interface TodayForYouItem {
    val key: String
    val media: TodayMedia
    val releasedAt: Instant

    @Immutable
    data class UpNext(
        val show: Show,
        val episode: Episode,
        override val releasedAt: Instant,
    ) : TodayForYouItem {
        override val media = TodayMedia.ShowMedia(show)
        override val key = "up-next-${show.ids.trakt.value}-${episode.ids.trakt.value}"
    }

    @Immutable
    data class StartWatching(
        override val media: TodayMedia,
        override val releasedAt: Instant,
    ) : TodayForYouItem {
        override val key = "start-watching-${media.key}"
    }

    companion object {
        fun upNext(
            show: Show,
            episode: Episode,
            window: ClosedRange<Instant>,
        ): TodayForYouItem? {
            val releasedAt = episode.releasedAt ?: return null
            if (releasedAt !in window) return null

            return UpNext(show = show, episode = episode, releasedAt = releasedAt)
        }

        fun startWatching(
            media: TodayMedia,
            window: ClosedRange<Instant>,
        ): TodayForYouItem? {
            val releasedAt = media.releasedAt ?: return null
            if (releasedAt !in window) return null

            return StartWatching(media = media, releasedAt = releasedAt)
        }

        private val TodayMedia.releasedAt: Instant?
            get() = when (this) {
                is TodayMedia.MovieMedia -> {
                    movie.released
                        ?.atStartOfDay(ZoneId.systemDefault())
                        ?.toInstant()
                }
                is TodayMedia.ShowMedia -> {
                    show.releasedAt
                }
            }
    }
}
