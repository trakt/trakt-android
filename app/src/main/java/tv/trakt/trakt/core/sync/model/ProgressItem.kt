package tv.trakt.trakt.core.sync.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.MovieProgress
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
        val lastWatchedAt: Instant,
        override val loading: Boolean = false,
    ) : ProgressItem(loading)

    @Immutable
    internal data class ShowItem(
        val show: Show,
        val seasons: ImmutableList<Season>,
        val progress: Progress,
        val lastWatchedAt: Instant,
        override val loading: Boolean = false,
    ) : ProgressItem(loading) {
        data class Show(
            val ids: Ids,
            val title: String,
        )

        data class Season(
            val number: Int,
            val episodes: ImmutableList<Episode>,
        )

        data class Episode(
            val number: Int,
            val plays: Int,
            val lastWatchedAt: Instant,
        )

        data class Progress(
            val aired: Int,
            val plays: Int,
            val lastWatchedAt: Instant?,
            val resetAt: Instant?,
        )

        fun isEpisodeWatched(
            season: Int,
            episode: Int,
        ): Boolean {
            return seasons
                .firstOrNull { it.number == season }
                ?.episodes
                ?.firstOrNull { it.number == episode }
                ?.let { it.plays > 0 }
                ?: false
        }

        val isCompleted: Boolean
            get() = progress.plays >= progress.aired
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
