package tv.trakt.trakt.core.summary.shows.features.progress.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import tv.trakt.trakt.common.core.sync.model.ProgressItem.ShowItem
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.TraktId

@Immutable
internal data class SeasonProgress(
    val number: Int,
    val watched: Int,
    val aired: Int,
    val total: Int,
    val watchedEpisodeIds: ImmutableSet<TraktId>,
) {
    val progress: Float
        get() = if (aired == 0) 0F else watched.toFloat() / aired

    val isComplete: Boolean
        get() = aired > 0 && watched >= aired

    val remaining: Int
        get() = aired - watched

    companion object {
        fun fromSeasons(
            seasons: List<Season>,
            progress: ShowItem?,
        ): ImmutableList<SeasonProgress> {
            val watchedBySeason = progress?.seasons
                ?.associate { season ->
                    season.number to season.episodes
                        .filter { it.plays.isNotEmpty() }
                        .map { it.id }
                        .toImmutableSet()
                }
                .orEmpty()

            return seasons
                .sortedBy { it.number }
                .map { season ->
                    val watchedIds = watchedBySeason[season.number] ?: persistentSetOf()
                    val aired = season.airedEpisodes ?: 0

                    SeasonProgress(
                        number = season.number,
                        watched = watchedIds.size.coerceAtMost(aired),
                        aired = aired,
                        total = season.episodeCount ?: 0,
                        watchedEpisodeIds = watchedIds,
                    )
                }
                .toImmutableList()
        }
    }
}
