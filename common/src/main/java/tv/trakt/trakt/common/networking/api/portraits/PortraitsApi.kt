package tv.trakt.trakt.common.networking.api.portraits

import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.get
import org.openapitools.client.infrastructure.ApiClient
import tv.trakt.trakt.common.data.dto.PortraitTokenDto

class PortraitsApi(
    private val baseUrl: String,
    httpClientEngine: HttpClientEngine,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit),
) : ApiClient(
        baseUrl,
        httpClientEngine,
        httpClientConfig,
    ) {
    suspend fun getToken(): PortraitTokenDto {
        return client.get("${baseUrl}portraits/token").body()
    }
}
