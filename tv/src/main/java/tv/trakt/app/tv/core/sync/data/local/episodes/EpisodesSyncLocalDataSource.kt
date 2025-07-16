package tv.trakt.app.tv.core.sync.data.local.episodes

import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.sync.model.WatchedEpisode
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
