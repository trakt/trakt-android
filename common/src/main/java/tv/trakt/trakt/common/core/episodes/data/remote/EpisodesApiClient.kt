package tv.trakt.trakt.common.core.episodes.data.remote

import org.openapitools.client.apis.EpisodeApi
import org.openapitools.client.apis.ShowsApi
import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.CastCrewDto
import tv.trakt.trakt.common.networking.CommentDto
import tv.trakt.trakt.common.networking.EpisodeCalendarsDto
import tv.trakt.trakt.common.networking.EpisodeStatsDto
import tv.trakt.trakt.common.networking.ExternalSeasonRatingsDto
import tv.trakt.trakt.common.networking.SyncHistoryEpisodeItemDto

internal class EpisodesApiClient(
    private val showsApi: ShowsApi,
    private val episodesApi: EpisodeApi,
    private val usersApi: UsersApi,
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

    override suspend fun getEpisodeComments(
        showId: TraktId,
        season: Int,
        episode: Int,
        limit: Int,
        sort: String,
        language: String?,
        extended: String,
    ): List<CommentDto> {
        val response = showsApi.getShowsEpisodeComments(
            id = showId.value.toString(),
            season = season,
            episode = episode,
            sort = sort,
            extended = extended,
            page = null,
            limit = limit.toString(),
            language = language,
        )
        return response.body()
    }

    override suspend fun getSeasonComments(
        showId: TraktId,
        season: Int,
        limit: Int,
        sort: String,
        language: String?,
    ): List<CommentDto> {
        val response = showsApi.getShowsSeasonComments(
            id = showId.value.toString(),
            season = season,
            sort = sort,
            extended = "full,images,vip",
            page = null,
            limit = limit.toString(),
            language = language,
        )
        return response.body()
    }

    override suspend fun getCastCrew(
        showId: TraktId,
        season: Int,
        episode: Int,
        guestStars: Boolean,
        extended: String?,
    ): CastCrewDto {
        val response = showsApi.getShowsEpisodePeople(
            id = showId.value.toString(),
            season = season,
            episode = episode,
            extended = extended ?: if (guestStars) "cloud9,full,guest_stars,markdown" else "cloud9,full,markdown",
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
            extended = "cloud9,full,markdown",
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
