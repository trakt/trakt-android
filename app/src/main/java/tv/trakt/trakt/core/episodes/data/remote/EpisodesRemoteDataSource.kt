package tv.trakt.trakt.core.episodes.data.remote

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.EpisodeDto

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
}
