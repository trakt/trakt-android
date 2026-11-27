package tv.trakt.trakt.common.networking.api.v3

import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import org.openapitools.client.infrastructure.ApiClient
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.api.v3.model.V3BulkIntlRequest
import tv.trakt.trakt.common.networking.api.v3.model.V3BulkIntlResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3DocumentaryCheckResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3ParentalGuideResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3RealityCheckResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3SentimentResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3SoundtrackTrackResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3TriviaResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3YouTubeSpecialResponse
import java.util.Locale
import kotlin.uuid.Uuid

class V3MediaInsightsApi(
    private val baseV3Url: String,
    httpClientEngine: HttpClientEngine,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit),
) : ApiClient(
        baseV3Url,
        httpClientEngine,
        httpClientConfig,
    ) {
    suspend fun getMovieSentiment(movieId: TraktId): V3SentimentResponse? {
        val response = client.get("${baseV3Url}media/movie/${movieId.value}/info/0/version/2")
        return response.body()
    }

    suspend fun getShowSentiment(showId: TraktId): V3SentimentResponse? {
        val response = client.get("${baseV3Url}media/show/${showId.value}/info/0/version/2")
        return response.body()
    }

    suspend fun getShowTrivia(
        showId: TraktId,
        locale: Locale,
    ): V3TriviaResponse {
        val response = client.get("${baseV3Url}media/show/${showId.value}/info/5/version/2") {
            parameter("locale", locale.toLanguageTag())
        }
        val body = response.body<V3TriviaResponse>()
        return body.copy(
            items = body.items?.map { it.copy(id = Uuid.random().toHexString()) },
        )
    }

    suspend fun getMovieTrivia(
        movieId: TraktId,
        locale: Locale,
    ): V3TriviaResponse {
        val response = client.get("${baseV3Url}media/movie/${movieId.value}/info/5/version/2") {
            parameter("locale", locale.toLanguageTag())
        }
        val body = response.body<V3TriviaResponse>()
        return body.copy(
            items = body.items?.map { it.copy(id = Uuid.random().toHexString()) },
        )
    }

    suspend fun getParentalGuide(
        type: MediaType,
        mediaId: TraktId,
    ): V3ParentalGuideResponse? {
        val response = client.get("${baseV3Url}media/${type.value}/${mediaId.value}/info/16/version/1")
        if (response.status == HttpStatusCode.NoContent) {
            return null
        }
        return response.body()
    }

    suspend fun getRealityCheck(
        type: MediaType,
        mediaId: TraktId,
        locale: Locale,
    ): V3RealityCheckResponse? {
        val response = client.get("${baseV3Url}media/${type.value}/${mediaId.value}/info/34/version/1") {
            parameter("locale", locale.toLanguageTag())
        }
        if (response.status == HttpStatusCode.NoContent) {
            return null
        }
        return response.body()
    }

    suspend fun getDocumentaryCheck(
        type: MediaType,
        mediaId: TraktId,
        locale: Locale,
    ): V3DocumentaryCheckResponse? {
        val response = client.get("${baseV3Url}media/${type.value}/${mediaId.value}/info/66/version/1") {
            parameter("locale", locale.toLanguageTag())
        }
        if (response.status == HttpStatusCode.NoContent) {
            return null
        }
        return response.body()
    }

    suspend fun getMovieYouTubeSpecial(
        movieId: TraktId,
        locale: Locale,
    ): V3YouTubeSpecialResponse? {
        val response = client.get("${baseV3Url}media/movie/${movieId.value}/info/17/version/1") {
            parameter("locale", locale.toLanguageTag())
        }
        if (response.status == HttpStatusCode.NoContent) {
            return null
        }
        return response.body()
    }

    suspend fun getBulkIntl(request: V3BulkIntlRequest): V3BulkIntlResponse {
        val response = client.get("${baseV3Url}intl/bulk") {
            parameter("language", request.language)
            parameter("country", request.country)
            request.movieIds.toCsv()?.let { parameter("m", it) }
            request.showIds.toCsv()?.let { parameter("s", it) }
            request.episodeIds.toCsv()?.let { parameter("e", it) }
        }
        return response.body()
    }

    private fun List<TraktId>.toCsv(): String? {
        return takeIf { it.isNotEmpty() }?.joinToString(",") { it.value.toString() }
    }

    suspend fun getSoundtrack(
        type: MediaType,
        mediaId: TraktId,
        locale: Locale,
    ): List<V3SoundtrackTrackResponse> {
        val response = client.get("${baseV3Url}media/${type.value}/${mediaId.value}/info/15/version/2") {
            parameter("locale", locale.toLanguageTag())
        }
        if (response.status == HttpStatusCode.NoContent) {
            return emptyList()
        }
        return response.body()
    }
}
