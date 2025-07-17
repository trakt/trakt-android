package tv.trakt.trakt.tv.core.details.episode.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.tv.common.model.SeasonEpisode
import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.tv.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.tv.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.tv.core.episodes.model.Episode
import tv.trakt.trakt.tv.core.episodes.model.fromDto
import tv.trakt.trakt.tv.helpers.extensions.asyncMap

internal class GetEpisodeSeasonUseCase(
    private val remoteSource: EpisodesRemoteDataSource,
    private val localEpisodeSource: EpisodeLocalDataSource,
) {
    suspend fun getEpisodeSeason(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
    ): ImmutableList<Episode> {
        val episodes = remoteSource.getEpisodeSeason(
            showId = showId,
            season = seasonEpisode.season,
        )
        return episodes
            .asyncMap { Episode.fromDto(it) }
            .sortedBy { it.number }
            .toImmutableList()
            .also {
                localEpisodeSource.upsertEpisodes(it)
            }
    }
}
