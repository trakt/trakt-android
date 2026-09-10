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
    /**
     * Reads the cached completed bucket, keeping only shows that have ended or only shows
     * still airing. Filtering happens before [limit] is applied so a caller asking for N
     * items is not short-changed by whatever the cache happens to hold first.
     */
    suspend fun getLocalCompleted(
        limit: Int,
        ended: Boolean,
    ): ImmutableList<ProfileProgressItem> {
        return localDataSource.getItems()
            .filterIsInstance<ProfileProgressItem.ShowItem>()
            .filter { it.show.hasEnded == ended }
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
            filters = null,
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
