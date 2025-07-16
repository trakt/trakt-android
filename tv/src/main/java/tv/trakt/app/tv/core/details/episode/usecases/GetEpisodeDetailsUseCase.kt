package tv.trakt.app.tv.core.details.episode.usecases

import tv.trakt.app.tv.common.model.SeasonEpisode
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.app.tv.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.app.tv.core.episodes.model.Episode
import tv.trakt.app.tv.core.episodes.model.fromDto

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
