package tv.trakt.trakt.app.core.streamings.data.local

import tv.trakt.trakt.app.core.streamings.model.StreamingSource

internal interface StreamingLocalDataSource {
    suspend fun getStreamingSource(sourceId: String): StreamingSource?

    suspend fun upsertStreamingSources(sources: List<StreamingSource>)

    suspend fun isValid(): Boolean
}
