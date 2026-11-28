package tv.trakt.trakt.common.networking.api.v3

import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import org.openapitools.client.infrastructure.ApiClient
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.networking.api.v3.model.V3FollowingActivityResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3MediaSocialResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3RecommendedByResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3ShareClickRequest
import tv.trakt.trakt.common.networking.api.v3.model.V3ShareClickResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3ShareMuteRequest
import java.time.Instant

class V3SocialApi(
    private val baseUrl: String,
    private val baseV3Url: String,
    httpClientEngine: HttpClientEngine,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit),
) : ApiClient(
        baseV3Url,
        httpClientEngine,
        httpClientConfig,
    ) {
    suspend fun getMovieSocialActivity(
        movieId: TraktId,
        pagination: Pagination,
    ): List<V3MediaSocialResponse>? {
        val response = client.get(
            "${baseUrl}movies/${movieId.value}/social" +
                "?page=${pagination.page}" +
                "&limit=${pagination.limit}",
        )
        return response.body()
    }

    suspend fun getShowSocialActivity(
        showId: TraktId,
        pagination: Pagination,
    ): List<V3MediaSocialResponse>? {
        val response = client.get(
            "${baseUrl}shows/${showId.value}/social" +
                "?page=${pagination.page}" +
                "&limit=${pagination.limit}",
        )
        return response.body()
    }

    suspend fun getEpisodeSocialActivity(
        showId: TraktId,
        season: Int,
        episode: Int,
        pagination: Pagination,
    ): List<V3MediaSocialResponse>? {
        val response = client.get(
            "${baseUrl}shows/${showId.value}/seasons/$season/episodes/$episode/social" +
                "?page=${pagination.page}" +
                "&limit=${pagination.limit}",
        )
        return response.body()
    }

    suspend fun getFollowingActivities(
        pagination: Pagination,
        window: ClosedRange<Instant>? = null,
    ): List<V3FollowingActivityResponse> {
        val response = client.get("${baseV3Url}users/me/following/activities") {
            parameter("extended", "full")
            parameter("page", pagination.page)
            parameter("limit", pagination.limit)
            window?.let {
                parameter("start_at", it.start.toString())
                parameter("end_at", it.endInclusive.toString())
            }
        }
        if (response.status == HttpStatusCode.NoContent) {
            return emptyList()
        }
        return response.body()
    }

    suspend fun postShareClick(request: V3ShareClickRequest): V3ShareClickResponse {
        val response = client.post("${baseV3Url}shares/click") {
            setBody(request)
        }
        return response.body()
    }

    suspend fun getRecommendedBy(url: String): V3RecommendedByResponse? {
        val response = client.get("${baseV3Url}shares/recommended") {
            parameter("url", url)
        }
        if (response.status == HttpStatusCode.NoContent) {
            return null
        }
        return response.body()
    }

    suspend fun postShareMute(request: V3ShareMuteRequest) {
        client.post("${baseV3Url}shares/mutes") {
            setBody(request)
        }
    }
}
