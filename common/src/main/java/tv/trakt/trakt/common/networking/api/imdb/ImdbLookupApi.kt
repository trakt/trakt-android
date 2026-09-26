package tv.trakt.trakt.common.networking.api.imdb

import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.get
import org.openapitools.client.infrastructure.ApiClient
import tv.trakt.trakt.common.networking.api.imdb.model.ImdbLookupItemDto

class ImdbLookupApi(
    private val baseUrl: String,
    httpClientEngine: HttpClientEngine,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit),
) : ApiClient(
        baseUrl,
        httpClientEngine,
        httpClientConfig,
    ) {
    suspend fun getByImdbId(imdbId: String): List<ImdbLookupItemDto> {
        return client.get("${baseUrl}search/imdb/$imdbId").body()
    }
}
