package tv.trakt.trakt.core.episodes.data.remote

import org.openapitools.client.apis.EpisodeApi
import org.openapitools.client.apis.ShowsApi
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.CastCrewDto
import tv.trakt.trakt.common.networking.CommentDto
import tv.trakt.trakt.common.networking.EpisodeDto
import tv.trakt.trakt.common.networking.ExternalRatingsDto
import tv.trakt.trakt.common.networking.StreamingDto

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

    override suspend fun getSeason(
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

    override suspend fun getStreamings(
        showId: TraktId,
        season: Int,
        episode: Int,
        countryCode: String?,
    ): Map<String, StreamingDto> {
        val response = showsApi.getShowsEpisodeWatchnow(
            country = countryCode ?: "",
            id = showId.value.toString(),
            season = season,
            episode = episode,
            links = "direct",
        )
        return response.body()
    }

    override suspend fun getEpisodeComments(
        showId: TraktId,
        season: Int,
        episode: Int,
        limit: Int,
        sort: String,
    ): List<CommentDto> {
        val response = showsApi.getShowsEpisodeComments(
            id = showId.value.toString(),
            season = season,
            episode = episode,
            sort = sort,
            extended = "images",
            page = null,
            limit = limit.toString(),
        )
        return response.body()
    }

    override suspend fun getCastCrew(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): CastCrewDto {
        val response = showsApi.getShowsEpisodePeople(
            id = showId.value.toString(),
            season = season,
            episode = episode,
            extended = "cloud9,full",
        )
        return response.body()
    }
}
