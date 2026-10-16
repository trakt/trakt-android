package tv.trakt.trakt.common.firebase.analytics.implementation

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import timber.log.Timber

internal fun interface HalSender {
    suspend fun send(body: String)
}

internal class KtorHalSender(
    private val client: HttpClient,
    private val endpoint: String,
    private val apiKey: String,
    private val userAgent: String,
) : HalSender {
    override suspend fun send(body: String) {
        val response = client.post(endpoint) {
            header(HEADER_API_KEY, apiKey)
            header(HttpHeaders.UserAgent, userAgent)
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        if (response.status != HttpStatusCode.NoContent) {
            Timber.d("HAL batch refused: status=%s", response.status.value)
        }
    }
}

private const val HEADER_API_KEY = "trakt-api-key"
