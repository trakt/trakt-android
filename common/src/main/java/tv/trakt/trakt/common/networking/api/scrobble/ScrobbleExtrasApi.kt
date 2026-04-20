package tv.trakt.trakt.common.networking.api.scrobble

import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.openapitools.client.infrastructure.ApiClient
import org.openapitools.client.models.GetSyncProgressEpisodesResponse
import org.openapitools.client.models.PostCheckinStartRequestOneOf1MovieIds
import org.openapitools.client.models.PostSyncRatingsRemoveRequestMoviesInner
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.api.scrobble.model.PostScrobbleMovieRequest

class ScrobbleExtrasApi(
    private val baseUrl: String,
    httpClientEngine: HttpClientEngine,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit),
) : ApiClient(
        baseUrl,
        httpClientEngine,
        httpClientConfig,
    ) {
    suspend fun postScrobbleMovieStart(
        movieId: TraktId,
        progress: Float,
    ) {
        client.post("${baseUrl}scrobble/start") {
            setBody(
                PostScrobbleMovieRequest(
                    progress = progress,
                    movie = PostSyncRatingsRemoveRequestMoviesInner(
                        ids = PostCheckinStartRequestOneOf1MovieIds(
                            trakt = movieId.value,
                            tmdb = 0,
                            imdb = null,
                            slug = null,
                        ),
                    ),
                ),
            )
        }
    }

    suspend fun postScrobbleMovieStop(
        movieId: TraktId,
        progress: Float,
    ) {
        client.post("${baseUrl}scrobble/stop") {
            setBody(
                PostScrobbleMovieRequest(
                    progress = progress,
                    movie = PostSyncRatingsRemoveRequestMoviesInner(
                        ids = PostCheckinStartRequestOneOf1MovieIds(
                            trakt = movieId.value,
                            tmdb = 0,
                            imdb = null,
                            slug = null,
                        ),
                    ),
                ),
            )
        }
    }

    suspend fun getSyncProgressEpisodes(
        page: Int?,
        limit: Int?,
    ): List<GetSyncProgressEpisodesResponse> {
        return client.get("${baseUrl}sync/playback/episodes") {
            parameter("page", page)
            parameter("limit", limit)
        }.body()
    }

    suspend fun deleteSyncPlayback(playbackId: Long) {
        client.delete("${baseUrl}sync/playback/$playbackId")
    }
}
