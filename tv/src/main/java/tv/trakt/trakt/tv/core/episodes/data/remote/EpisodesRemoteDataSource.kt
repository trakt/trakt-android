package tv.trakt.trakt.tv.core.episodes.data.remote

import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.tv.networking.openapi.CastCrewDto
import tv.trakt.trakt.tv.networking.openapi.CommentDto
import tv.trakt.trakt.tv.networking.openapi.EpisodeDto
import tv.trakt.trakt.tv.networking.openapi.ExternalRatingsDto
import tv.trakt.trakt.tv.networking.openapi.StreamingDto
import tv.trakt.trakt.tv.networking.openapi.SyncHistoryEpisodeItemDto

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
    ): EpisodeDto

    /**
     * Retrieves the external ratings for a specific episode of a show.
     *
     * @param showId The Trakt ID of the show.
     * @param season The season number.
     * @param episode The episode number.
     * @return An [ExternalRatingsDto] object containing the external ratings for the episode.
     */
    suspend fun getEpisodeExternalRatings(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): ExternalRatingsDto

    /**
     * Retrieves the streaming services available for a specific episode of a show in a given country.
     *
     * @param showId The Trakt ID of the show.
     * @param season The season number.
     * @param episode The episode number.
     * @param countryCode The two-letter country code (ISO 3166-1 alpha-2) for which to retrieve streaming services.
     * @return A map where the key is the streaming service provider and the value is a [StreamingDto] object containing details about the streaming offer.
     */
    suspend fun getEpisodeStreamings(
        showId: TraktId,
        season: Int,
        episode: Int,
        countryCode: String,
    ): Map<String, StreamingDto>

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
    ): List<EpisodeDto>

    /**
     * Retrieves the history of an episode.
     *
     * @param episodeId The Trakt ID of the episode.
     * @return A list of [SyncHistoryEpisodeItemDto] objects representing the watched history of the episode.
     */
    suspend fun getEpisodeHistory(episodeId: TraktId): List<SyncHistoryEpisodeItemDto>
}
