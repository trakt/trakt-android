package tv.trakt.trakt.common.networking.api.v3

import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.get
import org.openapitools.client.infrastructure.ApiClient
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.networking.api.v3.model.V3MediaSocialResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3MinimalList
import tv.trakt.trakt.common.networking.api.v3.model.V3MinimalWatchlistResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3SentimentResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3TriviaResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3UsageResponse

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
        return response.body<V3TriviaResponse>()
    }

    suspend fun getMovieTrivia(movieId: TraktId): V3TriviaResponse {
        val response = client.get("${baseV3Url}media/movie/${movieId.value}/info/5/version/1")
        return response.body<V3TriviaResponse>()
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
}
