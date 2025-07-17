package tv.trakt.trakt.tv.core.streamings.data.remote

import tv.trakt.trakt.tv.networking.openapi.StreamingSourceDto

internal interface StreamingRemoteDataSource {
    suspend fun getStreamingSources(countryCode: String): List<StreamingSourceDto>
}
