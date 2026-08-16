package tv.trakt.trakt.common.networking.api.klipy

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import tv.trakt.trakt.common.networking.api.klipy.model.KlipyGifDto
import tv.trakt.trakt.common.networking.api.klipy.model.KlipyGifsRequest
import tv.trakt.trakt.common.networking.api.klipy.model.KlipyPageDto
import tv.trakt.trakt.common.networking.api.klipy.model.KlipyResponse

/**
 * KLIPY GIF endpoints. The app key is part of the path, there is no auth header.
 * Docs: https://docs.klipy.com/gifs-api
 */
class KlipyApi(
    private val baseUrl: String,
    private val appKey: String,
    httpClientEngine: HttpClientEngine,
    httpClientConfig: (HttpClientConfig<*>) -> Unit,
) {
    private val client: HttpClient by lazy {
        HttpClient(httpClientEngine, httpClientConfig)
    }

    suspend fun getTrendingGifs(request: KlipyGifsRequest): KlipyPageDto<KlipyGifDto> {
        val response = client.get("$baseUrl$appKey/gifs/trending") {
            applyCommonParameters(request)
        }

        return response.body<KlipyResponse<KlipyPageDto<KlipyGifDto>>>().requireData()
    }

    suspend fun searchGifs(request: KlipyGifsRequest): KlipyPageDto<KlipyGifDto> {
        val response = client.get("$baseUrl$appKey/gifs/search") {
            applyCommonParameters(request)
            parameter("q", request.query)
        }

        return response.body<KlipyResponse<KlipyPageDto<KlipyGifDto>>>().requireData()
    }

    private fun HttpRequestBuilder.applyCommonParameters(request: KlipyGifsRequest) {
        parameter("page", request.page)
        parameter("per_page", request.perPage)
        parameter("customer_id", request.customerId)
        parameter("locale", request.locale)
        parameter("content_filter", request.contentFilter)
        parameter("format_filter", request.formatFilter)
    }

    private fun <T> KlipyResponse<T>.requireData(): T {
        val data = data
        if (!result || data == null) {
            throw KlipyApiException(message.orEmpty().ifEmpty { "KLIPY request failed" })
        }

        return data
    }
}

class KlipyApiException(
    message: String,
) : Exception(message)
