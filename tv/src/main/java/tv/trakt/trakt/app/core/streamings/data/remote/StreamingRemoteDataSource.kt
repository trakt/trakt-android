package tv.trakt.trakt.app.core.streamings.data.remote

import tv.trakt.trakt.common.networking.StreamingSourceDto

internal interface StreamingRemoteDataSource {
    suspend fun getStreamingSources(): List<StreamingSourceDto>
}
