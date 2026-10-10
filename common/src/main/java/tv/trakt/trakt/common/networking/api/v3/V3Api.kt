package tv.trakt.trakt.common.networking.api.v3

import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import org.openapitools.client.infrastructure.ApiClient
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.networking.api.v3.model.V3LeaderboardEntryResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3MediaRecommendationResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3MediaSocialResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3MinimalList
import tv.trakt.trakt.common.networking.api.v3.model.V3MinimalWatchlistResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3MovieRecommendationResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3ParentalGuideResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3RecommendationsRequest
import tv.trakt.trakt.common.networking.api.v3.model.V3RecommendedByResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3SentimentResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3ShareClickRequest
import tv.trakt.trakt.common.networking.api.v3.model.V3ShareClickResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3ShareMuteRequest
import tv.trakt.trakt.common.networking.api.v3.model.V3ShowRecommendationResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3TriviaResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3UsageResponse
import tv.trakt.trakt.common.networking.api.v3.model.reactions.V3MediaReaction
import tv.trakt.trakt.common.networking.api.v3.model.reactions.V3MediaReactionsSummaryResponse
import tv.trakt.trakt.common.networking.api.v3.model.reactions.V3UserMediaReaction
import kotlin.uuid.Uuid

class V3Api(
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

    suspend fun getWatchlistMinimal(): Pair<Set<TraktId>, Set<TraktId>> {
        val response = client.get("${baseV3Url}users/me/watchlist/minimal")
        val responseBody = response.body<V3MinimalWatchlistResponse>()

        val shows = responseBody.shows.orEmpty().map { it.toTraktId() }.toSet()
        val movies = responseBody.movies.orEmpty().map { it.toTraktId() }.toSet()

        return shows to movies
    }

    // Lists Management

    suspend fun getListsMinimal(): List<V3MinimalList> {
        val response = client.get("${baseV3Url}users/me/lists")
        return response.body()
    }

    suspend fun getMovieMeLists(movieId: TraktId): List<Int> {
        val response = client.get("${baseV3Url}movies/${movieId.value}/me/lists")
        return response.body<List<Int>>()
    }

    suspend fun getShowMeLists(showId: TraktId): List<Int> {
        val response = client.get("${baseV3Url}shows/${showId.value}/me/lists")
        return response.body<List<Int>>()
    }

    // Sentiment

    suspend fun getMovieSentiment(movieId: TraktId): V3SentimentResponse? {
        val response = client.get("${baseV3Url}media/movie/${movieId.value}/info/0/version/1")
        return response.body()
    }

    suspend fun getShowSentiment(showId: TraktId): V3SentimentResponse? {
        val response = client.get("${baseV3Url}media/show/${showId.value}/info/0/version/1")
        return response.body()
    }

    // Trivia

    suspend fun getShowTrivia(showId: TraktId): V3TriviaResponse {
        val response = client.get("${baseV3Url}media/show/${showId.value}/info/5/version/1")
        val body = response.body<V3TriviaResponse>()
        return body.copy(
            items = body.items?.map { it.copy(id = Uuid.random().toHexString()) },
        )
    }

    suspend fun getMovieTrivia(movieId: TraktId): V3TriviaResponse {
        val response = client.get("${baseV3Url}media/movie/${movieId.value}/info/5/version/1")
        val body = response.body<V3TriviaResponse>()
        return body.copy(
            items = body.items?.map { it.copy(id = Uuid.random().toHexString()) },
        )
    }

    // Parental Guide

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

    // Recommendations

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
        request.countries?.let { parameter("countries", it) }
        request.statuses?.let { parameter("statuses", it) }
        request.ignoreWatched?.let { parameter("ignore_watched", it) }
        request.ignoreWatchlisted?.let { parameter("ignore_watchlisted", it) }
        request.ignoreCollected?.let { parameter("ignore_collected", it) }
    }

    // Social

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

    // Leaderboard

    /** Only available for the authenticated user. Other users return 404. */
    suspend fun getLeaderboard(pagination: Pagination): List<V3LeaderboardEntryResponse> {
        val response = client.get(
            "${baseUrl}users/me/leaderboard" +
                "?page=${pagination.page}" +
                "&limit=${pagination.limit}",
        )
        return response.body()
    }

    // Shares

    suspend fun postShareClick(request: V3ShareClickRequest): V3ShareClickResponse {
        val response = client.post("${baseV3Url}shares/click") {
            setBody(request)
        }
        return response.body()
    }

    /**
     * @param url Relative web path of the item, e.g. `/movies/<slug>`.
     */
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

    // Reactions

    suspend fun getMediaReactions(): List<V3MediaReaction> {
        val response = client.get("${baseV3Url}reactions/media")
        return response.body()
    }

    suspend fun getMediaReactionsSummary(
        type: MediaType,
        mediaId: TraktId,
    ): V3MediaReactionsSummaryResponse {
        val response = client.get("${mediaReactionsUrl(type, mediaId)}/summary")
        return response.body()
    }

    suspend fun getUserMediaReactions(
        type: MediaType,
        mediaId: TraktId,
    ): List<V3UserMediaReaction> {
        val response = client.get("${baseV3Url}users/me/${type.value}/${mediaId.value}")
        return response.body()
    }

    suspend fun putMediaReactions(
        type: MediaType,
        mediaId: TraktId,
        reactions: List<String>,
    ): List<V3UserMediaReaction> {
        val response = client.put("${mediaReactionsUrl(type, mediaId)}/${reactions.joinToString(",")}")
        return response.body()
    }

    suspend fun deleteMediaReactions(
        type: MediaType,
        mediaId: TraktId,
        reactionIds: List<Long>,
    ) {
        client.delete("${mediaReactionsUrl(type, mediaId)}/${reactionIds.joinToString(",")}")
    }

    private fun mediaReactionsUrl(
        type: MediaType,
        mediaId: TraktId,
    ): String {
        return "${baseV3Url}${type.value}s/${mediaId.value}/reactions"
    }
}
