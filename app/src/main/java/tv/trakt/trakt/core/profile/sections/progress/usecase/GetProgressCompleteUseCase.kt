package tv.trakt.trakt.core.profile.sections.progress.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.remote.ShowsSyncRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.profile.ProfileProgressItem
import tv.trakt.trakt.core.profile.sections.progress.data.local.completed.ProgressCompletedLocalSource
import tv.trakt.trakt.core.profile.sections.progress.model.ProgressSorting

internal class GetProgressCompleteUseCase(
    private val remoteShowsSyncSource: ShowsSyncRemoteSource,
    private val localDataSource: ProgressCompletedLocalSource,
) {
    suspend fun getLocalCompleted(
        limit: Int,
        ended: Boolean,
    ): ImmutableList<ProfileProgressItem> {
        return localDataSource.getItems()
            .filterIsInstance<ProfileProgressItem.ShowItem>()
            .filter { it.show.isEnded == ended }
            .take(limit)
            .toImmutableList()
    }

    suspend fun getCompleted(
        page: Int,
        limit: Int,
        sorting: ProgressSorting,
    ): ImmutableList<ProfileProgressItem> {
        val remoteItems = remoteShowsSyncSource.getUpNext(
            limit = limit,
            page = page,
            intent = "completed",
            sortHow = sorting.order.value,
            sortBy = sorting.type.apiValue,
            filters = null,
        )

        val items = remoteItems
            .asyncMap {
                ProfileProgressItem.ShowItem(
                    show = Show.fromDto(it.show),
                )
            }

        return items
            .also {
                when (page) {
                    1 -> localDataSource.setItems(it)
                    else -> localDataSource.addItems(it)
                }
            }
            .toImmutableList()
    }
}
