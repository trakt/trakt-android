package tv.trakt.trakt.core.summary.episodes.features.history.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource

internal class GetEpisodeHistoryUseCase(
    private val remoteSource: UserRemoteDataSource,
) {
    suspend fun getHistory(episode: Episode): ImmutableList<HomeActivityItem.EpisodeItem> {
        return remoteSource.getEpisodeHistory(
            episodeId = episode.ids.trakt,
            limit = 100,
        ).asyncMap {
            HomeActivityItem.EpisodeItem(
                id = it.id,
                user = null,
                activity = it.action.value,
                activityAt = it.watchedAt.toInstant(),
                episode = Episode.fromDto(
                    checkNotNull(it.episode) {
                        "Episode should not be null"
                    },
                ),
                show = Show.fromDto(
                    checkNotNull(it.show) {
                        "Show should not be null for episode history"
                    },
                ),
            )
        }.filter {
            // Filter to ensure we only get history for the specified episode
            it.episode.ids.trakt == episode.ids.trakt
        }.sortedByDescending {
            it.activityAt
        }.toImmutableList()
    }
}
