package tv.trakt.trakt.core.summary.shows.features.history.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource

internal class GetShowHistoryUseCase(
    private val remoteSource: UserRemoteDataSource,
) {
    suspend fun getHistory(showId: TraktId): ImmutableList<HomeActivityItem.EpisodeItem> {
        return remoteSource.getShowHistory(
            showId = showId,
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
            // Filter episodes that belong to the specified show
            it.show.ids.trakt == showId
        }.sortedByDescending {
            it.activityAt
        }.toImmutableList()
    }
}
