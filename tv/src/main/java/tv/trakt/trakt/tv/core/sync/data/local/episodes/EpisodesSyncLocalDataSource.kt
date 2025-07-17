package tv.trakt.trakt.tv.core.sync.data.local.episodes

import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.tv.core.sync.model.WatchedEpisode
import java.time.ZonedDateTime

internal interface EpisodesSyncLocalDataSource {
    suspend fun saveHistory(
        episodes: List<WatchedEpisode>,
        timestamp: ZonedDateTime?,
    )

    suspend fun getHistory(): Map<TraktId, WatchedEpisode>?

    suspend fun getHistoryUpdatedAt(): ZonedDateTime?

    suspend fun clear(
        episodeIds: Set<TraktId>,
        timestamp: ZonedDateTime?,
    )

    suspend fun clear()
}
