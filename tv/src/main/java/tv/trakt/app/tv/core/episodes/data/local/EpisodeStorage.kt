package tv.trakt.app.tv.core.episodes.data.local

import kotlinx.collections.immutable.persistentMapOf
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.episodes.model.Episode
import java.util.concurrent.ConcurrentHashMap

// TODO Temporary cache implementation, replace with a proper database solution later.
internal class EpisodeStorage : EpisodeLocalDataSource {
    private val cache = ConcurrentHashMap<TraktId, Episode>(persistentMapOf())

    override suspend fun getEpisode(episodeId: TraktId): Episode? {
        return cache[episodeId]
    }

    override suspend fun upsertEpisodes(episodes: List<Episode>) {
        val map = episodes.associateBy { it.ids.trakt }
        cache.putAll(map)
    }
}
