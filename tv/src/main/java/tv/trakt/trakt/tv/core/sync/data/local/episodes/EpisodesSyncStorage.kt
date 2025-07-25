package tv.trakt.trakt.tv.core.sync.data.local.episodes

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.tv.core.sync.model.WatchedEpisode
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap

internal class EpisodesSyncStorage : EpisodesSyncLocalDataSource {
    private val mutex = Mutex()

    private var historyCache: MutableMap<TraktId, WatchedEpisode>? = null
    private var historyUpdatedAt: ZonedDateTime? = null

    override suspend fun saveHistory(
        episodes: List<WatchedEpisode>,
        timestamp: ZonedDateTime?,
    ) {
        mutex.withLock {
            if (episodes.isEmpty()) {
                return@withLock
            }

            if (historyCache == null) {
                historyCache = ConcurrentHashMap<TraktId, WatchedEpisode>()
            }

            historyCache?.run {
                val ids = episodes.map { e -> e.episodeId }
                entries.removeAll { it.key in ids }
                putAll(episodes.associateBy { it.episodeId })
            }
            timestamp?.let { historyUpdatedAt = it }
        }
    }

    override suspend fun getHistory(): Map<TraktId, WatchedEpisode>? {
        return mutex.withLock {
            historyCache?.toMap()
        }
    }

    override suspend fun getHistoryUpdatedAt(): ZonedDateTime? {
        return mutex.withLock {
            historyUpdatedAt
        }
    }

    override suspend fun clear(
        episodeIds: Set<TraktId>,
        timestamp: ZonedDateTime?,
    ) {
        mutex.withLock {
            historyCache?.run {
                entries.removeAll { it.key in episodeIds }
            }
            timestamp?.let {
                historyUpdatedAt = it
            }
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            historyUpdatedAt = null
            historyCache = null
        }
    }
}
