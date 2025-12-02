package tv.trakt.trakt.core.settings.features.younify.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.url
import tv.trakt.trakt.core.settings.features.younify.data.remote.model.dto.YounifyDetailsDto

internal class YounifyApiClient(
    private val baseUrl: String,
    httpClientEngine: HttpClientEngine,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit),
) : YounifyRemoteDataSource {
    private val httpClient = HttpClient(httpClientEngine) {
        httpClientConfig.invoke(this)
    }

    override suspend fun getYounifyDetails(generateTokens: Boolean): YounifyDetailsDto {
        val result = httpClient.post {
            url("$baseUrl/younify/users")
            parameter("generate_tokens", generateTokens)
        }

        return result.body()
    }
}
