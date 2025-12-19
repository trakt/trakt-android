package tv.trakt.trakt.core.billing.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import tv.trakt.trakt.core.billing.data.remote.model.VerifyPurchaseRequest

internal class BillingApiClient(
    private val baseUrl: String,
    httpClientEngine: HttpClientEngine,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit),
) : BillingRemoteDataSource {
    private val httpClient = HttpClient(httpClientEngine) {
        httpClientConfig.invoke(this)
    }

    override suspend fun verifyPurchase(request: VerifyPurchaseRequest) {
        httpClient.post {
            url("${baseUrl}vip/android/verify")
            setBody(request)
        }
    }
}
