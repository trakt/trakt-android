package tv.trakt.trakt.common.core.streamings.data.local

import kotlinx.collections.immutable.persistentMapOf
import tv.trakt.trakt.common.model.streamings.StreamingSource
import java.util.concurrent.ConcurrentHashMap

class StreamingStorage : StreamingLocalDataSource {
    private val cache = ConcurrentHashMap<String, StreamingSource>(persistentMapOf())

    override suspend fun getAllStreamingSources(): Map<String, StreamingSource> {
        return cache.toMap()
    }

    override suspend fun getStreamingSource(sourceId: String): StreamingSource? {
        return cache[sourceId]
    }

    override suspend fun upsertStreamingSources(sources: List<StreamingSource>) {
        val map = sources
            .distinctBy { it.source }
            .associateBy { it.source }
        cache.putAll(map)
    }

    override suspend fun isValid(): Boolean {
        return cache.isNotEmpty()
    }
}
