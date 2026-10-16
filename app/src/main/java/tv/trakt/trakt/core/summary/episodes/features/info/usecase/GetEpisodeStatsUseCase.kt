package tv.trakt.trakt.core.summary.episodes.features.info.usecase

import tv.trakt.trakt.common.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.EpisodeStatsDto

internal class GetEpisodeStatsUseCase(
    private val remoteSource: EpisodesRemoteDataSource,
) {
    suspend fun getStats(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): EpisodeStatsDto = remoteSource.getStats(showId, season, episode)
}
