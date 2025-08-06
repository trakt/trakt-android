package tv.trakt.trakt.app.core.streamings.data.local

import kotlinx.collections.immutable.persistentMapOf
import tv.trakt.trakt.app.core.streamings.model.StreamingSource
import java.util.concurrent.ConcurrentHashMap

internal class StreamingStorage : StreamingLocalDataSource {
    private val cache = ConcurrentHashMap<String, StreamingSource>(persistentMapOf())

    override suspend fun getAllStreamingSources(sourceId: String): Map<String, StreamingSource> {
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
