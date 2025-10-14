package tv.trakt.trakt.app.core.home.sections.shows.upnext.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import java.time.ZonedDateTime

@Immutable
internal data class ProgressShow(
    val progress: Progress,
    val show: Show,
) {
    val id: TraktId
        get() = progress.nextEpisode.ids.trakt
}

@Immutable
internal data class Progress(
    val lastWatchedAt: ZonedDateTime?,
    val aired: Int,
    val completed: Int,
    val stats: Stats?,
    val nextEpisode: Episode,
    val lastEpisode: Episode?,
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

    val remainingMinutesString: String?
        get() {
            return stats?.minutesLeft?.toLong()?.durationFormat()
        }
}
