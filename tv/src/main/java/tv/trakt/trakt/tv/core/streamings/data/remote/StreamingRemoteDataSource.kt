package tv.trakt.trakt.tv.core.streamings.data.remote

import tv.trakt.trakt.common.networking.StreamingSourceDto

internal interface StreamingRemoteDataSource {
    suspend fun getStreamingSources(countryCode: String): List<StreamingSourceDto>
}
