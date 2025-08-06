package tv.trakt.trakt.app.core.streamings.data.remote

import org.openapitools.client.apis.WatchnowApi
import tv.trakt.trakt.common.networking.StreamingSourceDto

internal class StreamingApiClient(
    private val api: WatchnowApi,
) : StreamingRemoteDataSource {
    override suspend fun getStreamingSources(): List<StreamingSourceDto> {
        val response = api.getWatchnowSourcesCountry("")

        return response.body()
            .flatMap { it.values.flatten() }
            .filter { it.source.isNotBlank() && it.linkCount > 0 }
            .distinctBy { it.source }
    }
}
