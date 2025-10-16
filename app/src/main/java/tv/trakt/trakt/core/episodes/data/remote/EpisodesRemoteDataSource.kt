package tv.trakt.trakt.core.episodes.data.remote

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.CastCrewDto
import tv.trakt.trakt.common.networking.EpisodeDto
import tv.trakt.trakt.common.networking.ExternalRatingsDto
import tv.trakt.trakt.common.networking.StreamingDto

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
    ): EpisodeDto

    /**
     * Retrieves all episodes for a specific season of a show.
     *
     * @return A list of [EpisodeDto] objects representing the episodes in the season.
     */
    suspend fun getSeason(
        showId: TraktId,
        season: Int,
    ): List<EpisodeDto>

    /**
     * Retrieves the external ratings for a specific episode of a show.
     *
     * @return An [ExternalRatingsDto] object containing the external ratings for the episode.
     */
    suspend fun getExternalRatings(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): ExternalRatingsDto

    /**
     * Retrieves the streaming services available for a specific episode of a show in a given country.
     *
     * @return A map where the key is the streaming service provider and the value is a [StreamingDto] object containing details about the streaming offer.
     */
    suspend fun getStreamings(
        showId: TraktId,
        season: Int,
        episode: Int,
        countryCode: String?,
    ): Map<String, StreamingDto>

    /**
     * Retrieves the cast and crew for a specific episode of a show.
     */
    suspend fun getCastCrew(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): CastCrewDto
}
