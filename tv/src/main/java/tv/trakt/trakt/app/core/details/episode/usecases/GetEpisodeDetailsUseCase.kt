package tv.trakt.trakt.app.core.details.episode.usecases

import tv.trakt.trakt.app.common.model.SeasonEpisode
import tv.trakt.trakt.app.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.app.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.app.core.episodes.model.Episode
import tv.trakt.trakt.app.core.episodes.model.fromDto
import tv.trakt.trakt.common.model.TraktId

internal class GetEpisodeDetailsUseCase(
    private val localSource: EpisodeLocalDataSource,
    private val remoteSource: EpisodesRemoteDataSource,
) {
    suspend fun getEpisodeDetails(
        showId: TraktId,
        episodeId: TraktId,
        seasonEpisode: SeasonEpisode,
    ): Episode? {
        val localEpisode = localSource.getEpisode(episodeId)
        if (localEpisode != null) {
            return localEpisode
        }

        val remoteEpisode = remoteSource.getEpisodeDetails(
            showId = showId,
            season = seasonEpisode.season,
            episode = seasonEpisode.episode,
        )

        return Episode.fromDto(remoteEpisode)
            .also {
                localSource.upsertEpisodes(listOf(it))
            }
    }
}
