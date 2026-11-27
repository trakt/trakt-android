package tv.trakt.trakt.common.networking.api.v3

import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodeURLPathPart
import org.openapitools.client.infrastructure.ApiClient
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.networking.api.v3.model.V3LeaderboardEntryResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3LeaderboardResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3NudgeResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3PushTokenRequest
import tv.trakt.trakt.common.networking.api.v3.model.V3SyncConnectionResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3UsageResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3UserMatchResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3UsernameSettingsRequest
import tv.trakt.trakt.common.networking.api.v3.model.V3WatchedPlayResponse

class V3UserApi(
    private val baseUrl: String,
    private val baseV3Url: String,
    httpClientEngine: HttpClientEngine,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit),
) : ApiClient(
        baseV3Url,
        httpClientEngine,
        httpClientConfig,
    ) {
    suspend fun getUsage(): V3UsageResponse {
        val response = client.get("${baseV3Url}users/me/usage")
        return response.body()
    }

    suspend fun getNudge(): V3NudgeResponse? {
        val response = client.get("${baseV3Url}users/me/nudge")
        if (response.status == HttpStatusCode.NoContent) {
            return null
        }
        return response.body()
    }

    suspend fun postNudgeSeen() {
        client.post("${baseV3Url}users/me/nudge/seen")
    }

    suspend fun getSyncConnections(): List<V3SyncConnectionResponse> {
        val response = client.get("${baseV3Url}users/me/sync/connections")
        return response.body()
    }

    suspend fun getUserMatch(
        userSlug: String,
        type: String?,
    ): V3UserMatchResponse {
        val response = client.get("${baseV3Url}users/${userSlug.encodeURLPathPart()}/match") {
            type?.let { parameter("type", it) }
        }
        return response.body()
    }

    suspend fun getLeaderboard(pagination: Pagination): V3LeaderboardResponse {
        val response = client.get(
            "${baseUrl}users/me/leaderboard" +
                "?page=${pagination.page}" +
                "&limit=${pagination.limit}",
        )
        return V3LeaderboardResponse(
            entries = response.body<List<V3LeaderboardEntryResponse>>(),
            totalCount = response.headers["x-pagination-item-count"]?.toIntOrNull(),
        )
    }

    suspend fun putUsername(username: String) {
        client.put("${baseUrl}users/settings") {
            setBody(
                V3UsernameSettingsRequest(
                    user = V3UsernameSettingsRequest.User(username = username),
                ),
            )
        }
    }

    suspend fun postPushToken(request: V3PushTokenRequest) {
        client.post("${baseUrl}users/token") {
            setBody(request)
        }
    }

    suspend fun getWatchedMoviePlays(): Map<String, List<V3WatchedPlayResponse>> {
        val response = client.get("${baseV3Url}users/me/watched/movies/plays") {
            parameter("limit", "all")
        }
        return response.body()
    }

    suspend fun getWatchedShowPlays(): Map<String, Map<String, Map<String, List<V3WatchedPlayResponse>>>> {
        val response = client.get("${baseV3Url}users/me/watched/shows/plays") {
            parameter("limit", "all")
        }
        return response.body()
    }
}
