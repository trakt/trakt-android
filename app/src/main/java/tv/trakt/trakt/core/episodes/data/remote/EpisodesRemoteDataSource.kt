package tv.trakt.trakt.core.episodes.data.remote

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.CastCrewDto
import tv.trakt.trakt.common.networking.CommentDto
import tv.trakt.trakt.common.networking.EpisodeCalendarsDto
import tv.trakt.trakt.common.networking.EpisodeDto
import tv.trakt.trakt.common.networking.EpisodeStatsDto
import tv.trakt.trakt.common.networking.ExternalSeasonRatingsDto
import tv.trakt.trakt.common.networking.ExternalShowRatingsDto

internal interface EpisodesRemoteDataSource {
    /**
     * Retrieves the details for a specific episode of a show.
     *
     * @return An [EpisodeDto] object containing the details of the episode.
     */
    suspend fun getEpisodeDetails(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): EpisodeCalendarsDto

    /**
     * Retrieves all episodes for a specific season of a show.
     *
     * @return A list of [EpisodeDto] objects representing the episodes in the season.
     */
    suspend fun getSeason(
        showId: TraktId,
        season: Int,
    ): List<EpisodeCalendarsDto>

    /**
     * Retrieves the external ratings for a specific episode of a show.
     *
     * @return An [ExternalShowRatingsDto] object containing the external ratings for the episode.
     */
    suspend fun getExternalRatings(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): ExternalSeasonRatingsDto

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
    ): List<CommentDto>

    /**
     * Retrieves the comments for a specific season of a show.
     *
     * @param showId The Trakt ID of the show.
     * @param season The season number.
     * @param limit The maximum number of comments to retrieve.
     * @param sort The sort order for comments (e.g., "likes", "newest").
     * @return A list of [CommentDto] objects representing the comments for the season.
     */
    suspend fun getSeasonComments(
        showId: TraktId,
        season: Int,
        limit: Int = 20,
        sort: String = "likes",
    ): List<CommentDto>

    /**
     * Retrieves the cast and crew for a specific episode of a show.
     */
    suspend fun getCastCrew(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): CastCrewDto

    /**
     * Retrieves the cast and crew for a specific season of a show.
     */
    suspend fun getCastCrew(
        showId: TraktId,
        season: Int,
    ): CastCrewDto

    /**
     * Retrieves the stats for a specific episode of a show.
     */
    suspend fun getStats(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): EpisodeStatsDto
}
