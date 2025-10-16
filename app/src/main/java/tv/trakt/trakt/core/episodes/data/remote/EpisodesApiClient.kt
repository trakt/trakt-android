package tv.trakt.trakt.core.episodes.data.remote

import org.openapitools.client.apis.EpisodeApi
import org.openapitools.client.apis.ShowsApi
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.EpisodeDto
import tv.trakt.trakt.common.networking.ExternalRatingsDto

internal class EpisodesApiClient(
    private val showsApi: ShowsApi,
    private val episodesApi: EpisodeApi,
) : EpisodesRemoteDataSource {
    override suspend fun getEpisodeDetails(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): EpisodeDto {
        val response = showsApi.getShowsEpisodeSummary(
            id = showId.value.toString(),
            season = season,
            episode = episode,
            extended = "full,cloud9",
        )
        return response.body()
    }

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

    override suspend fun getExternalRatings(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): ExternalRatingsDto {
        val response = episodesApi.getShowsEpisodeRatings(
            id = showId.value.toString(),
            season = season,
            episode = episode,
            extended = "all",
        )
        return response.body()
    }
}
