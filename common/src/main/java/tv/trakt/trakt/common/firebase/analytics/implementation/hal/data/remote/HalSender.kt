package tv.trakt.trakt.common.firebase.analytics.implementation.hal.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import timber.log.Timber

internal enum class HalDelivery {
    Accepted,
    Rejected,
    Failed,
}

internal fun interface HalSender {
    suspend fun send(body: String): HalDelivery
}

internal class KtorHalSender(
    private val client: HttpClient,
    private val endpoint: String,
    private val apiKey: String,
    private val userAgent: String,
) : HalSender {
    override suspend fun send(body: String): HalDelivery {
        val response = client.post(endpoint) {
            header(HEADER_API_KEY, apiKey)
            header(HttpHeaders.UserAgent, userAgent)
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        val status = response.status
        return when {
            status.isSuccess() -> {
                HalDelivery.Accepted
            }

            status == HttpStatusCode.TooManyRequests || status.value >= 500 -> {
                HalDelivery.Failed
            }

            else -> {
                Timber.d("HAL batch refused: status=%s", status.value)
                HalDelivery.Rejected
            }
        }
    }
}

private const val HEADER_API_KEY = "trakt-api-key"
