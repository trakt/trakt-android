package tv.trakt.trakt.app.core.home.sections.shows.upnext.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant
import kotlin.math.ceil

@Immutable
internal data class ProgressMovie(
    val movie: Movie,
    val progress: Progress,
) : ProgressItem {
    override val id: TraktId
        get() = movie.ids.trakt

    override val key: String
        get() = "${movie.ids.trakt.value}-movie"

    override val sortKey: String
        get() = "${progress.pausedAt}-${movie.title}"

    @Composable
    fun remainingTimeText(): String? {
        val runtime = movie.runtime?.inWholeMinutes ?: return null
        val progressPercent = progress.progress / 100F
        val remainingMinutes = ceil((1F - progressPercent) * runtime).toLong()
        return rememberDurationFormat(remainingMinutes)
    }

    @Immutable
    internal data class Progress(
        val id: Long,
        val progress: Float,
        val pausedAt: Instant,
    )
}
