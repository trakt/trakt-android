package tv.trakt.trakt.core.sync.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.MovieProgress
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant

@Immutable
internal sealed class ProgressItem(
    open val plays: Int,
    open val lastWatchedAt: Instant,
    open val lastUpdatedAt: Instant,
    open val loading: Boolean,
) {
    @Immutable
    internal data class MovieItem(
        override val plays: Int,
        override val lastWatchedAt: Instant,
        override val lastUpdatedAt: Instant,
        override val loading: Boolean = false,
        val movie: MovieProgress,
    ) : ProgressItem(plays, lastWatchedAt, lastUpdatedAt, loading)

    @Immutable
    internal data class ShowItem(
        override val plays: Int,
        override val lastWatchedAt: Instant,
        override val lastUpdatedAt: Instant,
        override val loading: Boolean = false,
        val show: Show,
    ) : ProgressItem(plays, lastWatchedAt, lastUpdatedAt, loading)

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
