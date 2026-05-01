package tv.trakt.trakt.common.networking.api.scrobble

import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.openapitools.client.infrastructure.ApiClient
import org.openapitools.client.models.GetSyncProgressEpisodesResponse

class ScrobbleExtrasApi(
    private val baseUrl: String,
    httpClientEngine: HttpClientEngine,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit),
) : ApiClient(
        baseUrl,
        httpClientEngine,
        httpClientConfig,
    ) {
    suspend fun getSyncProgressEpisodes(
        page: Int?,
        limit: Int?,
    ): List<GetSyncProgressEpisodesResponse> {
        return client.get("${baseUrl}sync/playback/episodes") {
            parameter("page", page)
            parameter("limit", limit)
        }.body()
    }

    suspend fun deleteSyncPlayback(playbackId: Long) {
        client.delete("${baseUrl}sync/playback/$playbackId")
    }
}
