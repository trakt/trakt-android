package tv.trakt.trakt.app.core.sync.data.local.episodes

import tv.trakt.trakt.app.core.sync.model.WatchedEpisode
import tv.trakt.trakt.common.model.TraktId
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
