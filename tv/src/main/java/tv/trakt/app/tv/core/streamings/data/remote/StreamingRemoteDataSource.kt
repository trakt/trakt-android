package tv.trakt.app.tv.core.streamings.data.remote

import tv.trakt.app.tv.networking.openapi.StreamingSourceDto

internal interface StreamingRemoteDataSource {
    suspend fun getStreamingSources(countryCode: String): List<StreamingSourceDto>
}
