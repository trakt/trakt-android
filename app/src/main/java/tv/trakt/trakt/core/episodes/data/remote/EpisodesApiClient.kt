package tv.trakt.trakt.core.episodes.data.remote

import org.openapitools.client.apis.ShowsApi
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.EpisodeDto

internal class EpisodesApiClient(
    private val showsApi: ShowsApi,
) : EpisodesRemoteDataSource {
    override suspend fun getEpisodeSeason(
        showId: TraktId,
        season: Int,
    ): List<EpisodeDto> {
        val response = showsApi.getShowsSeasonEpisodes(
            id = showId.value.toString(),
            season = season,
            extended = "full,cloud9",
        )
        return response.body()
    }
}
