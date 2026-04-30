package tv.trakt.trakt.core.sync.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.MovieProgress
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant
import tv.trakt.trakt.common.model.Show as ShowCommon

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
        val showId: TraktId,
        val seasons: ImmutableList<Season>,
        override val loading: Boolean = false,
    ) : ProgressItem(loading) {
        val plays: Int
            get() = seasons
                .flatMap { it.episodes }
                .sumOf { it.plays }

        val playsDistinct: Int
            get() = seasons
                .flatMap { it.episodes }
                .sumOf { it.playsDistinct }

        val lastWatchedAt: Instant
            get() = seasons
                .flatMap { it.episodes }
                .maxBy { it.lastWatchedAt }
                .lastWatchedAt

        data class Season(
            val id: TraktId,
            val number: Int,
            val episodes: ImmutableList<Episode>,
        )

        data class Episode(
            val id: TraktId,
            val plays: Int,
            val playsDistinct: Int,
            val lastWatchedAt: Instant,
        )

        fun isEpisodeWatched(
            seasonNumber: Int,
            episodeId: TraktId,
        ): Boolean {
            return seasons
                .firstOrNull { it.number == seasonNumber }
                ?.episodes
                ?.firstOrNull { it.id == episodeId }
                ?.let { it.plays > 0 }
                ?: false
        }

        fun isCompleted(show: ShowCommon): Boolean {
            val airedEpisodes = show.airedEpisodes

            val watchedEpisodes = seasons
                .flatMap { it.episodes }
                .filter { it.plays > 0 }

            return watchedEpisodes.size >= airedEpisodes
        }
    }

    val mediaId: TraktId
        get() = when (this) {
            is ShowItem -> showId
            is MovieItem -> movie.ids.trakt
        }

    val key: String
        get() = when (this) {
            is ShowItem -> "${showId.value}-progress-show"
            is MovieItem -> "${movie.ids.trakt.value}-progress-movie"
        }
}
