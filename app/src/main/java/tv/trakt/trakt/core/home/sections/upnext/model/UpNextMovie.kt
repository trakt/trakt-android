package tv.trakt.trakt.core.home.sections.upnext.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.MediaType.MOVIE
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant
import kotlin.math.ceil

@Immutable
internal data class UpNextMovie(
    val movie: Movie,
    val progress: Progress,
    override val type: MediaType = MOVIE,
    override val loading: Boolean = false,
) : UpNextItem {
    override val id: TraktId
        get() = movie.ids.trakt

    override val mediaId: TraktId
        get() = movie.ids.trakt

    override val key: String
        get() = "up_next_movie-${movie.ids.trakt.value}"

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
