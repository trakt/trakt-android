package tv.trakt.trakt.tv.core.episodes.data.remote

import org.openapitools.client.apis.ShowsApi
import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.common.networking.CastCrewDto
import tv.trakt.trakt.common.networking.CommentDto
import tv.trakt.trakt.common.networking.EpisodeDto
import tv.trakt.trakt.common.networking.ExternalRatingsDto
import tv.trakt.trakt.common.networking.StreamingDto
import tv.trakt.trakt.common.networking.SyncHistoryEpisodeItemDto
import tv.trakt.trakt.tv.common.model.TraktId

internal class EpisodesApiClient(
    private val showsApi: ShowsApi,
    private val usersApi: UsersApi,
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

    override suspend fun getEpisodeExternalRatings(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): ExternalRatingsDto {
        val response = showsApi.getShowsEpisodeRatings(
            id = showId.value.toString(),
            season = season,
            episode = episode,
            extended = "all",
        )
        return response.body()
    }

    override suspend fun getEpisodeStreamings(
        showId: TraktId,
        season: Int,
        episode: Int,
        countryCode: String,
    ): Map<String, StreamingDto> {
        val response = showsApi.getShowsEpisodeWatchnow(
            country = countryCode,
            id = showId.value.toString(),
            season = season,
            episode = episode,
            links = "direct",
        )
        return response.body()
    }

    override suspend fun getEpisodeCastCrew(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): CastCrewDto {
        val response = showsApi.getShowsEpisodePeople(
            id = showId.value.toString(),
            season = season,
            episode = episode,
            extended = "cloud9",
        )
        return response.body()
    }

    override suspend fun getEpisodeComments(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): List<CommentDto> {
        val response = showsApi.getShowsEpisodeComments(
            id = showId.value.toString(),
            season = season,
            episode = episode,
            sort = "likes",
            extended = "images",
            page = null,
            limit = 20.toString(),
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

    override suspend fun getEpisodeHistory(episodeId: TraktId): List<SyncHistoryEpisodeItemDto> {
        val response = usersApi.getUsersHistoryEpisode(
            id = "me",
            itemId = episodeId.value.toString(),
            extended = "full,images",
            startAt = null,
            endAt = null,
            page = 1,
            limit = 50,
        )

        return response.body()
    }
}
