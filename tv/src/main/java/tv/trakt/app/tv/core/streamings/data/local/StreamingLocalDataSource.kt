package tv.trakt.app.tv.core.streamings.data.local

import tv.trakt.app.tv.core.streamings.model.StreamingSource

internal interface StreamingLocalDataSource {
    suspend fun getStreamingSource(sourceId: String): StreamingSource?

    suspend fun upsertStreamingSources(sources: List<StreamingSource>)

    suspend fun isValid(): Boolean
}
