package tv.trakt.trakt.core.sync.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.MovieProgress
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant

@Immutable
internal sealed class ProgressItem(
    open val loading: Boolean,
) {
    @Immutable
    internal data class MovieItem(
        val movie: MovieProgress,
        val plays: Int,
        override val loading: Boolean = false,
    ) : ProgressItem(loading)

    @Immutable
    internal data class ShowItem(
        val show: Show,
        val progress: Progress,
        override val loading: Boolean = false,
    ) : ProgressItem(loading) {
        data class Progress(
            val aired: Int,
            val completed: Int,
            val plays: Int?,
            val lastWatchedAt: Instant?,
            val resetAt: Instant?,
        )

        val isCompleted: Boolean
            get() = progress.completed >= progress.aired
    }

    val mediaId: TraktId
        get() = when (this) {
            is ShowItem -> show.ids.trakt
            is MovieItem -> movie.ids.trakt
        }

    val key: String
        get() = when (this) {
            is ShowItem -> "${show.ids.trakt.value}-progress-show"
            is MovieItem -> "${movie.ids.trakt.value}-progress-movie"
        }
}
