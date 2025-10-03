package tv.trakt.trakt.common.core.streamings.data.remote

import tv.trakt.trakt.common.networking.StreamingSourceDto

interface StreamingRemoteDataSource {
    suspend fun getStreamingSources(): List<StreamingSourceDto>
}
