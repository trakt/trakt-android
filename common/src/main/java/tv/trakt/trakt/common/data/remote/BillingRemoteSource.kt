package tv.trakt.trakt.common.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import tv.trakt.trakt.common.data.request.VerifyPurchaseRequest

class BillingRemoteSource(
    private val baseUrl: String,
    httpClientEngine: HttpClientEngine,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit),
) {
    private val httpClient = HttpClient(httpClientEngine) {
        httpClientConfig.invoke(this)
    }

    suspend fun verifyPurchase(request: VerifyPurchaseRequest) {
        httpClient.post {
            url("${baseUrl}vip/android/verify")
            setBody(request)
        }
    }

    fun clear() {
        httpClient.authProvider<BearerAuthProvider>()?.clearToken()
    }
}
