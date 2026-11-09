package tv.trakt.trakt.core.summary.shows.features.progress

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.summary.shows.features.progress.model.SeasonProgress

@Immutable
internal data class ShowProgressState(
    val show: Show? = null,
    val seasons: ImmutableList<SeasonProgress>? = null,
    val episodes: ImmutableMap<Int, ImmutableList<Episode>> = persistentMapOf(),
    val expanded: ImmutableMap<Int, Boolean> = persistentMapOf(),
    val upNext: UpNext? = null,
    val watchedEpisodes: Int = 0,
    val busyEpisodes: ImmutableSet<TraktId> = persistentSetOf(),
    val loadingSeasons: ImmutableSet<Int> = persistentSetOf(),
    val error: Exception? = null,
) {
    @Immutable
    data class UpNext(
        val episodeId: TraktId,
        val season: Int,
    )

    val totalEpisodes: Int
        get() = show?.airedEpisodes ?: 0

    val watchedCount: Int
        get() = watchedEpisodes.coerceAtMost(totalEpisodes)

    val fraction: Float
        get() = if (totalEpisodes == 0) 0F else watchedCount.toFloat() / totalEpisodes

    val isWatched: Boolean
        get() = totalEpisodes > 0 && watchedCount >= totalEpisodes

    val currentSeason: Int?
        get() = resolveCurrentSeason(seasons, upNext)

    fun isExpanded(season: Int): Boolean {
        if (!episodes.containsKey(season)) return false
        return expanded[season] ?: (season == currentSeason)
    }
}

internal fun resolveCurrentSeason(
    seasons: List<SeasonProgress>?,
    upNext: ShowProgressState.UpNext?,
): Int? {
    return upNext?.season ?: seasons
        ?.firstOrNull { it.number != 0 && it.aired > 0 && !it.isComplete }
        ?.number
}
