package tv.trakt.trakt.core.summary.shows.features.history.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.user.data.remote.history.UserHistoryRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem

internal const val HISTORY_PAGE_LIMIT = 100

internal class GetShowHistoryUseCase(
    private val remoteSource: UserHistoryRemoteDataSource,
) {
    suspend fun getHistory(
        showId: TraktId,
        pagination: Pagination,
    ): ImmutableList<HomeActivityItem.EpisodeItem> {
        return remoteSource.getShowHistory(
            showId = showId,
            pagination = pagination,
        ).asyncMap {
            HomeActivityItem.EpisodeItem(
                id = it.id,
                user = null,
                userRating = null,
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
