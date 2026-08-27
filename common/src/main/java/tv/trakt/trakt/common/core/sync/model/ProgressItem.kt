package tv.trakt.trakt.common.core.sync.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.MovieProgress
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant
import tv.trakt.trakt.common.model.Show as ShowCommon

@Immutable
sealed class ProgressItem(
    open val loading: Boolean,
) {
    @Immutable
    data class MovieItem(
        val movie: MovieProgress,
        val plays: ImmutableList<Instant>,
        val lastWatchedAt: Instant,
        override val loading: Boolean = false,
    ) : ProgressItem(loading)

    @Immutable
    data class ShowItem(
        val showId: TraktId,
        val seasons: ImmutableList<Season>,
        override val loading: Boolean = false,
    ) : ProgressItem(loading) {
        val plays: Int
            get() = seasons
                .flatMap { it.episodes }
                .sumOf { it.plays.size }

        val playsWithoutSpecials: Int
            get() = seasons
                .filter { !it.isSpecial }
                .flatMap { it.episodes }
                .sumOf { it.plays.size }

        val playsDistinctWithoutSpecials: Int
            get() = seasons
                .filter { !it.isSpecial }
                .flatMap { it.episodes }
                .sumOf { it.playsDistinct }

        data class Season(
            val id: TraktId,
            val number: Int,
            val episodes: ImmutableList<Episode>,
        ) {
            val isSpecial: Boolean
                get() = number == 0
        }

        data class Episode(
            val id: TraktId,
            val special: Boolean,
            val plays: ImmutableList<Instant>,
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
                ?.plays
                ?.isNotEmpty()
                ?: false
        }

        fun isCompleted(show: ShowCommon): Boolean {
            val watchedEpisodes = seasons
                .flatMap { it.episodes }
                .filter { !it.special && it.plays.isNotEmpty() }

            return watchedEpisodes.size >= show.airedEpisodes
        }
    }

    val mediaId: TraktId
        get() = when (this) {
            is ShowItem -> showId
            is MovieItem -> movie.ids.trakt
        }
}
