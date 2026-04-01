package tv.trakt.trakt.common.networking.api.v3

import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.get
import org.openapitools.client.infrastructure.ApiClient
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.networking.api.v3.model.V3MinimalList
import tv.trakt.trakt.common.networking.api.v3.model.V3MinimalWatchlistResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3TriviaResponse

class V3Api(
    private val baseUrl: String,
    httpClientEngine: HttpClientEngine,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit),
) : ApiClient(
        baseUrl,
        httpClientEngine,
        httpClientConfig,
    ) {
    suspend fun getWatchlistMinimal(): Pair<Set<TraktId>, Set<TraktId>> {
        val response = client.get("${baseUrl}users/me/watchlist/minimal")
        val responseBody = response.body<V3MinimalWatchlistResponse>()

        val shows = responseBody.shows.orEmpty().map { it.toTraktId() }.toSet()
        val movies = responseBody.movies.orEmpty().map { it.toTraktId() }.toSet()

        return shows to movies
    }

    // Lists Management

    suspend fun getListsMinimal(): List<V3MinimalList> {
        val response = client.get("${baseUrl}users/me/lists")
        return response.body()
    }

    suspend fun getMovieMeLists(movieId: TraktId): List<Int> {
        val response = client.get("${baseUrl}movies/${movieId.value}/me/lists")
        return response.body<List<Int>>()
    }

    suspend fun getShowMeLists(showId: TraktId): List<Int> {
        val response = client.get("${baseUrl}shows/${showId.value}/me/lists")
        return response.body<List<Int>>()
    }

    // Trivia

    suspend fun getShowTrivia(showId: TraktId): V3TriviaResponse {
        val response = client.get("${baseUrl}media/show/${showId.value}/info/5/version/1")
        return response.body<V3TriviaResponse>()
    }

    suspend fun getMovieTrivia(movieId: TraktId): V3TriviaResponse {
        val response = client.get("${baseUrl}media/movie/${movieId.value}/info/5/version/1")
        return response.body<V3TriviaResponse>()
    }
}
