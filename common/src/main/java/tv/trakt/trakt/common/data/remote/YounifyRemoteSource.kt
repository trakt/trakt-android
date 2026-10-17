package tv.trakt.trakt.common.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.request.delete
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import tv.trakt.trakt.common.data.dto.YounifyDetailsDto
import tv.trakt.trakt.common.data.request.YounifyRefreshRequest

class YounifyRemoteSource(
    private val baseUrl: String,
    httpClientEngine: HttpClientEngine,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit),
) {
    private val httpClient = HttpClient(httpClientEngine) {
        httpClientConfig.invoke(this)
    }

    suspend fun getYounifyDetails(generateTokens: Boolean): YounifyDetailsDto {
        val result = httpClient.post {
            url("${baseUrl}younify/users")
            parameter("generate_tokens", generateTokens)
        }

        return result.body()
    }

    suspend fun postYounifyRefresh(
        serviceId: String,
        skipSync: Boolean,
    ) {
        httpClient.post {
            url("${baseUrl}younify/users/refresh")
            setBody(
                YounifyRefreshRequest(
                    serviceID = serviceId,
                    skipSync = skipSync,
                ),
            )
        }
    }

    suspend fun postYounifyUnlink(serviceId: String) {
        httpClient.delete {
            url("${baseUrl}younify/users/services/$serviceId")
        }
    }

    suspend fun clear() {
        httpClient.authProvider<BearerAuthProvider>()?.clearToken()
    }
}
