package tv.trakt.trakt.common.data.local

import kotlinx.collections.immutable.persistentMapOf
import tv.trakt.trakt.common.model.streamings.StreamingSource
import java.util.concurrent.ConcurrentHashMap

class StreamingLocalSource {
    private val cache = ConcurrentHashMap<String, StreamingSource>(persistentMapOf())

    suspend fun getAllStreamingSources(): Map<String, StreamingSource> {
        return cache.toMap()
    }

    suspend fun getStreamingSource(sourceId: String): StreamingSource? {
        return cache[sourceId]
    }

    suspend fun upsertStreamingSources(sources: List<StreamingSource>) {
        val map = sources
            .distinctBy { it.source }
            .associateBy { it.source }
        cache.putAll(map)
    }

    suspend fun isValid(): Boolean {
        return cache.isNotEmpty()
    }
}
