package tv.trakt.trakt.core.profile.sections.progress.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.profile.sections.progress.data.local.completed.ProgressCompletedLocalDataSource
import tv.trakt.trakt.core.profile.sections.progress.model.ProfileProgressItem
import tv.trakt.trakt.core.sync.data.remote.shows.ShowsSyncRemoteDataSource

internal class GetProgressCompleteUseCase(
    private val remoteShowsSyncSource: ShowsSyncRemoteDataSource,
    private val localDataSource: ProgressCompletedLocalDataSource,
) {
    suspend fun getLocalCompleted(limit: Int): ImmutableList<ProfileProgressItem> {
        return localDataSource.getItems()
            .filterIsInstance<ProfileProgressItem.ShowItem>()
            .take(limit)
            .toImmutableList()
    }

    suspend fun getCompleted(
        page: Int,
        limit: Int,
    ): ImmutableList<ProfileProgressItem> {
        val remoteItems = remoteShowsSyncSource.getUpNext(
            limit = limit,
            page = page,
            intent = "completed",
            sortHow = null,
            sortBy = null,
        )

        return remoteItems
            .asyncMap {
                ProfileProgressItem.ShowItem(
                    show = Show.fromDto(it.show),
                )
            }
            .also {
                when (page) {
                    1 -> localDataSource.setItems(it)
                    else -> localDataSource.addItems(it)
                }
            }
            .toImmutableList()
    }
}
