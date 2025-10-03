package tv.trakt.trakt.common.core.streamings.data.local

import tv.trakt.trakt.common.model.streamings.StreamingSource

interface StreamingLocalDataSource {
    suspend fun getAllStreamingSources(): Map<String, StreamingSource>

    suspend fun getStreamingSource(sourceId: String): StreamingSource?

    suspend fun upsertStreamingSources(sources: List<StreamingSource>)

    suspend fun isValid(): Boolean
}
