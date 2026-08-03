package tv.trakt.trakt.core.episodes.data.remote

import org.openapitools.client.apis.EpisodeApi
import org.openapitools.client.apis.ShowsApi
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.CastCrewDto
import tv.trakt.trakt.common.networking.CommentDto
import tv.trakt.trakt.common.networking.EpisodeCalendarsDto
import tv.trakt.trakt.common.networking.EpisodeStatsDto
import tv.trakt.trakt.common.networking.ExternalSeasonRatingsDto
import tv.trakt.trakt.common.networking.StreamingDto

internal class EpisodesApiClient(
    private val showsApi: ShowsApi,
    private val episodesApi: EpisodeApi,
) : EpisodesRemoteDataSource {
    override suspend fun getEpisodeDetails(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): EpisodeCalendarsDto {
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
    ): List<EpisodeCalendarsDto> {
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
    ): ExternalSeasonRatingsDto {
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
            extended = "streaming_ranks",
        )
        return response.body()
    }

    override suspend fun getJustWatchLink(
        showId: TraktId,
        season: Int,
        countryCode: String,
    ): String? {
        val response = showsApi.getShowsSeasonJustwatchLink(
            season = season.toString(),
            country = countryCode,
            id = showId.value.toString(),
        )
        return response.body()[countryCode]
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
            extended = "full,images,vip",
            page = null,
            limit = limit.toString(),
            language = null,
        )
        return response.body()
    }

    override suspend fun getSeasonComments(
        showId: TraktId,
        season: Int,
        limit: Int,
        sort: String,
    ): List<CommentDto> {
        val response = showsApi.getShowsSeasonComments(
            id = showId.value.toString(),
            season = season,
            sort = sort,
            extended = "full,images,vip",
            page = null,
            limit = limit.toString(),
            language = null,
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

    override suspend fun getCastCrew(
        showId: TraktId,
        season: Int,
    ): CastCrewDto {
        val response = showsApi.getShowsSeasonPeople(
            id = showId.value.toString(),
            season = season,
            extended = "cloud9,full",
        )
        return response.body()
    }

    override suspend fun getStats(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): EpisodeStatsDto {
        val response = showsApi.getShowsEpisodeStats(
            id = showId.value.toString(),
            season = season,
            episode = episode,
        )
        return response.body()
    }
}
