package tv.trakt.trakt.core.summary.episodes.usecases

import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.episodes.data.remote.EpisodesRemoteDataSource

internal class GetEpisodeDetailsUseCase(
    private val remoteSource: EpisodesRemoteDataSource,
    private val localSource: EpisodeLocalDataSource,
) {
    suspend fun getLocalEpisode(episodeId: TraktId): Episode? {
        return localSource.getEpisode(episodeId)
    }

    suspend fun getEpisode(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
    ): Episode? {
        return remoteSource.getEpisodeDetails(
            showId = showId,
            season = seasonEpisode.season,
            episode = seasonEpisode.episode,
        ).let {
            Episode.fromDto(it)
        }.also {
            localSource.upsertEpisodes(listOf(it))
        }
    }
}
