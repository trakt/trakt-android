package tv.trakt.trakt.common.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId

class EpisodeLocalSource {
    private val mutex = Mutex()
    private val storage = mutableMapOf<TraktId, Episode>()

    suspend fun getEpisode(episodeId: TraktId): Episode? {
        return mutex.withLock {
            storage[episodeId]
        }
    }

    suspend fun upsertEpisodes(episodes: List<Episode>) {
        mutex.withLock {
            with(storage) {
                putAll(episodes.associateBy { it.ids.trakt })
            }
        }
    }
}
