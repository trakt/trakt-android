package tv.trakt.trakt.common.core.streamings.data.remote

import org.openapitools.client.apis.MoviesApi
import org.openapitools.client.apis.ShowsApi
import org.openapitools.client.apis.WatchnowApi
import tv.trakt.trakt.common.core.streamings.model.StreamingsRequest
import tv.trakt.trakt.common.model.MediaType.Episode
import tv.trakt.trakt.common.model.MediaType.Movie
import tv.trakt.trakt.common.model.MediaType.Season
import tv.trakt.trakt.common.model.MediaType.Show
import tv.trakt.trakt.common.networking.StreamingDto
import tv.trakt.trakt.common.networking.StreamingSourceDto

private const val LINKS = "direct"
private const val EXTENDED = "streaming_ranks"

class StreamingApiClient(
    private val api: WatchnowApi,
    private val showsApi: ShowsApi,
    private val moviesApi: MoviesApi,
) : StreamingRemoteDataSource {
    override suspend fun getStreamingSources(): List<StreamingSourceDto> {
        val response = api.getWatchnowSourcesCountry("")

        return response.body()
            .flatMap { it.values.flatten() }
            .filter { it.source.isNotBlank() && it.linkCount > 0 }
            .distinctBy { it.source }
    }

    override suspend fun getStreamings(request: StreamingsRequest): Map<String, StreamingDto> {
        val id = request.mediaId.value.toString()
        val country = request.countryCode.orEmpty()

        val response = when (request.mediaType) {
            Show -> showsApi.getShowsWatchnow(
                id = id,
                country = country,
                links = LINKS,
                extended = EXTENDED,
            )

            Movie -> moviesApi.getMoviesWatchnow(
                id = id,
                country = country,
                links = LINKS,
                extended = EXTENDED,
            )

            Episode -> showsApi.getShowsEpisodeWatchnow(
                id = id,
                country = country,
                season = request.seasonEpisode?.season ?: 0,
                episode = request.seasonEpisode?.episode ?: 0,
                links = LINKS,
                extended = EXTENDED,
            )

            Season -> error("Unsupported media type: ${request.mediaType}")
        }

        return response.body()
    }

    override suspend fun getJustWatchLink(request: StreamingsRequest): String? {
        val id = request.mediaId.value.toString()
        val country = request.countryCode ?: return null

        val response = when (request.mediaType) {
            Show -> showsApi.getShowsJustwatchLink(
                country = country,
                id = id,
            )

            Movie -> moviesApi.getMoviesJustwatchLink(
                country = country,
                id = id,
            )

            // Episodes share their season's JustWatch page.
            Episode -> showsApi.getShowsSeasonJustwatchLink(
                season = (request.seasonEpisode?.season ?: 0).toString(),
                country = country,
                id = id,
            )

            Season -> error("Unsupported media type: ${request.mediaType}")
        }

        return response.body()[country]
    }
}
