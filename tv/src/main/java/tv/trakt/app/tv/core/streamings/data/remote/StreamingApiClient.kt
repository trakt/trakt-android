package tv.trakt.app.tv.core.streamings.data.remote

import org.openapitools.client.apis.WatchnowApi
import tv.trakt.app.tv.networking.openapi.StreamingSourceDto

internal class StreamingApiClient(
    private val api: WatchnowApi,
) : StreamingRemoteDataSource {
    override suspend fun getStreamingSources(countryCode: String): List<StreamingSourceDto> {
        val response = api.getWatchnowSourcesCountry(countryCode)

        return response.body()
            .firstOrNull()
            ?.getOrDefault(countryCode, emptyList())
            ?: emptyList()
    }
}
