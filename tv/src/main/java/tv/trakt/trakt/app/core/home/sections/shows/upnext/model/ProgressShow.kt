package tv.trakt.trakt.app.core.home.sections.shows.upnext.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.toTraktId
import java.time.Instant
import java.time.ZonedDateTime

@Immutable
internal data class ProgressShow(
    val progress: Progress,
    val show: Show,
) : ProgressItem {
    override val id: TraktId
        get() = progress.nextEpisode?.ids?.trakt ?: 0.toTraktId()

    override val key: String
        get() = "${show.ids.trakt.value}-${progress.nextEpisode?.ids?.trakt}-show"

    override val sortKey: String
        get() = "${(progress.lastWatchedAt?.toInstant() ?: Instant.MAX)}-${show.title}"

    @Immutable
    data class Progress(
        val lastWatchedAt: ZonedDateTime?,
        val aired: Int,
        val completed: Int,
        val stats: Stats?,
        val nextEpisode: Episode?,
        val lastEpisode: Episode?,
        val isLatestAired: Boolean = false,
    ) {
        @Immutable
        internal data class Stats(
            val playCount: Int,
            val minutesWatched: Int,
            val minutesLeft: Int?,
        )

        val remainingEpisodes: Int
            get() {
                return (aired - completed).coerceAtLeast(0)
            }

        val remainingPercent: Float
            get() {
                if (aired == 0) return 0F
                return completed.toFloat() / aired.toFloat()
            }
    }
}
