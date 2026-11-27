package tv.trakt.trakt.common.networking.api.v3

import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import org.openapitools.client.infrastructure.ApiClient
import tv.trakt.trakt.common.data.dto.MediaMatchDto
import tv.trakt.trakt.common.data.dto.MixtapeDto
import tv.trakt.trakt.common.data.dto.MixtapeMediaDto
import tv.trakt.trakt.common.data.dto.MixtapeVectorsDto
import tv.trakt.trakt.common.data.request.MixtapeRequest
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.api.v3.model.V3MediaRecommendationResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3MovieRecommendationResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3RecommendationReasonsResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3RecommendationsRequest
import tv.trakt.trakt.common.networking.api.v3.model.V3ShowRecommendationResponse

class V3RecommendationsApi(
    private val baseUrl: String,
    private val baseV3Url: String,
    httpClientEngine: HttpClientEngine,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit),
) : ApiClient(
        baseV3Url,
        httpClientEngine,
        httpClientConfig,
    ) {
    suspend fun getMovieRecommendations(request: V3RecommendationsRequest): List<V3MovieRecommendationResponse> {
        val response = client.get("${baseUrl}movies/recommendations") {
            applyRecommendationsRequest(request)
        }
        return response.body()
    }

    suspend fun getShowRecommendations(request: V3RecommendationsRequest): List<V3ShowRecommendationResponse> {
        val response = client.get("${baseUrl}shows/recommendations") {
            applyRecommendationsRequest(request)
        }
        return response.body()
    }

    suspend fun getMediaRecommendations(request: V3RecommendationsRequest): List<V3MediaRecommendationResponse> {
        val response = client.get("${baseUrl}media/recommendations") {
            applyRecommendationsRequest(request)
        }
        return response.body()
    }

    suspend fun getMediaMatch(
        type: MediaType,
        mediaId: TraktId,
    ): MediaMatchDto {
        val response = client.get("${baseUrl}${type.value}s/${mediaId.value}/match")
        return response.body()
    }

    suspend fun getRecommendationReasons(
        type: MediaType,
        mediaId: TraktId,
    ): V3RecommendationReasonsResponse {
        val response = client.get("${baseV3Url}recommendations/${type.value}/${mediaId.value}/reasons")
        return response.body()
    }

    suspend fun getMixtapeHistory(
        type: MediaType,
        query: String?,
    ): List<MixtapeMediaDto> {
        val response = client.get("${baseV3Url}mixtape/history") {
            parameter("type", type.value)
            parameter("extended", "full")
            query?.let { parameter("query", it) }
        }
        if (response.status == HttpStatusCode.NoContent) {
            return emptyList()
        }
        return response.body()
    }

    suspend fun getMixtapeVectors(
        type: MediaType,
        ids: List<TraktId>,
    ): MixtapeVectorsDto? {
        val response = client.get("${baseV3Url}mixtape/vectors") {
            parameter("type", type.value)
            parameter("ids", ids.joinToString(",") { it.value.toString() })
        }
        if (response.status == HttpStatusCode.NoContent) {
            return null
        }
        return response.body()
    }

    suspend fun postMixtape(request: MixtapeRequest): MixtapeDto {
        val response = client.post("${baseV3Url}mixtape") {
            parameter("extended", "full")
            setBody(request)
        }
        return response.body()
    }

    private fun HttpRequestBuilder.applyRecommendationsRequest(request: V3RecommendationsRequest) {
        parameter("limit", request.limit)
        request.extended?.let { parameter("extended", it) }
        request.watchWindow?.let { parameter("watch_window", it) }
        request.watchnow?.let { parameter("watchnow", it) }
        request.genres?.let { parameter("genres", it) }
        request.subgenres?.let { parameter("subgenres", it) }
        request.years?.let { parameter("years", it) }
        request.ratings?.let { parameter("ratings", it) }
        request.runtimes?.let { parameter("runtimes", it) }
        request.certifications?.let { parameter("certifications", it) }
        request.parentalNudity?.let { parameter("parental_nudity", it) }
        request.parentalViolence?.let { parameter("parental_violence", it) }
        request.parentalProfanity?.let { parameter("parental_profanity", it) }
        request.parentalAlcohol?.let { parameter("parental_alcohol", it) }
        request.parentalFrightening?.let { parameter("parental_frightening", it) }
        request.countries?.let { parameter("countries", it) }
        request.statuses?.let { parameter("statuses", it) }
        request.ignoreWatched?.let { parameter("ignore_watched", it) }
        request.ignoreWatchlisted?.let { parameter("ignore_watchlisted", it) }
        request.ignoreCollected?.let { parameter("ignore_collected", it) }
    }
}
