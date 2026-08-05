package tv.trakt.trakt.app.core.episodes.data.remote

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.CastCrewDto
import tv.trakt.trakt.common.networking.CommentDto
import tv.trakt.trakt.common.networking.EpisodeCalendarsDto
import tv.trakt.trakt.common.networking.EpisodeDto
import tv.trakt.trakt.common.networking.ExternalSeasonRatingsDto
import tv.trakt.trakt.common.networking.ExternalShowRatingsDto
import tv.trakt.trakt.common.networking.SyncHistoryEpisodeItemDto

internal interface EpisodesRemoteDataSource {
    /**
     * Retrieves the details for a specific episode of a show.
     *
     * @param showId The Trakt ID of the show.
     * @param season The season number.
     * @param episode The episode number.
     * @return An [EpisodeDto] object containing the details of the episode.
     */
    suspend fun getEpisodeDetails(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): EpisodeCalendarsDto

    /**
     * Retrieves the external ratings for a specific episode of a show.
     *
     * @param showId The Trakt ID of the show.
     * @param season The season number.
     * @param episode The episode number.
     * @return An [ExternalShowRatingsDto] object containing the external ratings for the episode.
     */
    suspend fun getEpisodeExternalRatings(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): ExternalSeasonRatingsDto

    /**
     * Retrieves the cast and crew for a specific episode of a show.
     *
     * @param showId The Trakt ID of the show.
     * @param season The season number.
     * @param episode The episode number.
     * @return A [CastCrewDto] object containing the cast and crew information for the episode.
     */
    suspend fun getEpisodeCastCrew(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): CastCrewDto

    /**
     * Retrieves the comments for a specific episode of a show.
     *
     * @param showId The Trakt ID of the show.
     * @param season The season number.
     * @param episode The episode number.
     * @return A list of [CommentDto] objects representing the comments for the episode.
     */
    suspend fun getEpisodeComments(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): List<CommentDto>

    /**
     * Retrieves all episodes for a specific season of a show.
     *
     * @param showId The Trakt ID of the show.
     * @param season The season number.
     * @return A list of [EpisodeDto] objects representing the episodes in the season.
     */
    suspend fun getEpisodeSeason(
        showId: TraktId,
        season: Int,
    ): List<EpisodeCalendarsDto>

    /**
     * Retrieves the history of an episode.
     *
     * @param episodeId The Trakt ID of the episode.
     * @return A list of [SyncHistoryEpisodeItemDto] objects representing the watched history of the episode.
     */
    suspend fun getEpisodeHistory(episodeId: TraktId): List<SyncHistoryEpisodeItemDto>
}
