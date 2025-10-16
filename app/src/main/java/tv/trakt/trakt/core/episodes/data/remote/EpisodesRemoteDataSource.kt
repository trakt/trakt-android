package tv.trakt.trakt.core.episodes.data.remote

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.CastCrewDto
import tv.trakt.trakt.common.networking.EpisodeDto
import tv.trakt.trakt.common.networking.ExternalRatingsDto

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
     * Retrieves the external ratings for a specific episode of a show.
     *
     * @param showId The Trakt ID of the show.
     * @param season The season number.
     * @param episode The episode number.
     * @return An [ExternalRatingsDto] object containing the external ratings for the episode.
     */
    suspend fun getExternalRatings(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): ExternalRatingsDto

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
}
