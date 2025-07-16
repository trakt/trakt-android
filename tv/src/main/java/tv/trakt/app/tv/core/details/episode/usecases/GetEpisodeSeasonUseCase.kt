package tv.trakt.app.tv.core.details.episode.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.app.tv.common.model.SeasonEpisode
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.app.tv.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.app.tv.core.episodes.model.Episode
import tv.trakt.app.tv.core.episodes.model.fromDto
import tv.trakt.app.tv.helpers.extensions.asyncMap

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
