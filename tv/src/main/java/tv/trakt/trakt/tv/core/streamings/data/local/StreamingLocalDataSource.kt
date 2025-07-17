package tv.trakt.trakt.tv.core.streamings.data.local

import tv.trakt.trakt.tv.core.streamings.model.StreamingSource

internal interface StreamingLocalDataSource {
    suspend fun getStreamingSource(sourceId: String): StreamingSource?

    suspend fun upsertStreamingSources(sources: List<StreamingSource>)

    suspend fun isValid(): Boolean
}
