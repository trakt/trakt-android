package tv.trakt.trakt.tv.core.details.episode.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.tv.common.model.SyncHistoryEpisodeItem
import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.tv.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.tv.core.episodes.model.Episode
import tv.trakt.trakt.tv.core.episodes.model.fromDto
import tv.trakt.trakt.tv.core.shows.model.Show
import tv.trakt.trakt.tv.core.shows.model.fromDto
import tv.trakt.trakt.tv.core.sync.data.local.episodes.EpisodesSyncLocalDataSource
import tv.trakt.trakt.tv.core.sync.model.WatchedEpisode
import tv.trakt.trakt.tv.helpers.extensions.asyncMap
import tv.trakt.trakt.tv.helpers.extensions.toZonedDateTime
import java.time.ZonedDateTime

internal class GetEpisodeHistoryUseCase(
    private val remoteSource: EpisodesRemoteDataSource,
    private val syncLocalSource: EpisodesSyncLocalDataSource,
) {
    suspend fun getEpisodeHistory(
        episodeId: TraktId,
        timestamp: ZonedDateTime?,
    ): ImmutableList<SyncHistoryEpisodeItem> {
        val history = remoteSource.getEpisodeHistory(
            episodeId = episodeId,
        )
        return history
            .asyncMap {
                SyncHistoryEpisodeItem(
                    id = it.id,
                    watchedAt = it.watchedAt.toZonedDateTime(),
                    episode = Episode.fromDto(it.episode),
                    show = Show.fromDto(it.show),
                )
            }
            .sortedByDescending { it.watchedAt }
            .toImmutableList()
            .also {
                val episodes = it.asyncMap { item ->
                    WatchedEpisode(
                        episodeId = item.episode.ids.trakt,
                        lastWatchedAt = item.watchedAt,
                    )
                }.distinctBy { e -> e.episodeId }

                with(syncLocalSource) {
                    clear(episodes.map { e -> e.episodeId }.toSet(), timestamp)
                    saveHistory(episodes, timestamp)
                }
            }
    }
}
