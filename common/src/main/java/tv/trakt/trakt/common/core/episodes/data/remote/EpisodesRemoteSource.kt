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

class EpisodesRemoteSource(
    private val showsApi: ShowsApi,
    private val episodesApi: EpisodeApi,
    private val usersApi: UsersApi,
) {
    /**
     * Retrieves the details for a specific episode of a show.
     *
     * @return An [EpisodeDto] object containing the details of the episode.
     */
    suspend fun getEpisodeDetails(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): EpisodeCalendarsDto {
        val response = showsApi.getShowsEpisodeSummary(
            id = showId.value.toString(),
            season = season,
            episode = episode,
            extended = "full,images",
        )
        return response.body()
    }

    /**
     * Retrieves all episodes for a specific season of a show.
     *
     * @return A list of [EpisodeDto] objects representing the episodes in the season.
     */
    suspend fun getSeason(
        showId: TraktId,
        season: Int,
    ): List<EpisodeCalendarsDto> {
        val response = showsApi.getShowsSeasonEpisodes(
            id = showId.value.toString(),
            season = season,
            extended = "full,images",
        )
        return response.body()
    }

    /**
     * Retrieves the external ratings for a specific episode of a show.
     *
     * @return An [ExternalShowRatingsDto] object containing the external ratings for the episode.
     */
    suspend fun getExternalRatings(
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

    /**
     * Retrieves the comments for a specific episode of a show.
     *
     * @param showId The Trakt ID of the show.
     * @param season The season number.
     * @param episode The episode number.
     * @param limit The maximum number of comments to retrieve.
     * @param sort The sort order for comments (e.g., "likes", "newest").
     * @return A list of [CommentDto] objects representing the comments for the episode.
     */
    suspend fun getEpisodeComments(
        showId: TraktId,
        season: Int,
        episode: Int,
        limit: Int = 20,
        sort: String = "likes",
        language: String? = null,
        extended: String = "full,images,vip",
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

    /**
     * Retrieves the comments for a specific season of a show.
     *
     * @param showId The Trakt ID of the show.
     * @param season The season number.
     * @param limit The maximum number of comments to retrieve.
     * @param sort The sort order for comments (e.g., "likes", "newest").
     * @param language The language code to filter comments by, or null for all languages.
     * @return A list of [CommentDto] objects representing the comments for the season.
     */
    suspend fun getSeasonComments(
        showId: TraktId,
        season: Int,
        limit: Int = 20,
        sort: String = "likes",
        language: String? = null,
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

    /**
     * Retrieves the cast and crew for a specific episode of a show.
     */
    suspend fun getCastCrew(
        showId: TraktId,
        season: Int,
        episode: Int,
        guestStars: Boolean = false,
        extended: String? = null,
    ): CastCrewDto {
        val response = showsApi.getShowsEpisodePeople(
            id = showId.value.toString(),
            season = season,
            episode = episode,
            extended = extended ?: if (guestStars) "images,full,guest_stars,markdown" else "images,full,markdown",
        )
        return response.body()
    }

    /**
     * Retrieves the cast and crew for a specific season of a show.
     */
    suspend fun getCastCrew(
        showId: TraktId,
        season: Int,
    ): CastCrewDto {
        val response = showsApi.getShowsSeasonPeople(
            id = showId.value.toString(),
            season = season,
            extended = "images,full,markdown",
        )
        return response.body()
    }

    /**
     * Retrieves the stats for a specific episode of a show.
     */
    suspend fun getStats(
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

    /**
     * Retrieves the current user's watch history for a specific episode.
     */
    suspend fun getEpisodeHistory(episodeId: TraktId): List<SyncHistoryEpisodeItemDto> {
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
