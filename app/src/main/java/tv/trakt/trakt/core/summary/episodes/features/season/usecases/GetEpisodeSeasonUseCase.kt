package tv.trakt.trakt.core.summary.episodes.features.season.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.core.summary.shows.features.seasons.model.EpisodeItem

internal class GetEpisodeSeasonUseCase(
    private val remoteEpisodesSource: EpisodesRemoteDataSource,
    private val localEpisodeSource: EpisodeLocalDataSource,
) {
    suspend fun getSeasonEpisodes(
        showId: TraktId,
        seasonNumber: Int,
    ): ImmutableList<EpisodeItem> {
        return remoteEpisodesSource.getSeason(showId, seasonNumber)
            .asyncMap {
                EpisodeItem(
                    episode = Episode.Companion.fromDto(it),
                )
            }
            .toImmutableList()
            .also {
                val episodes = it.asyncMap { item -> item.episode }
                localEpisodeSource.upsertEpisodes(episodes)
            }
    }
}
