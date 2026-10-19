package tv.trakt.trakt.core.profile.sections.progress.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.remote.ShowsSyncRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.profile.ProfileProgressItem
import tv.trakt.trakt.core.profile.sections.progress.data.local.watching.ProgressWatchingLocalSource

internal class GetProgressWatchingUseCase(
    private val remoteShowsSyncSource: ShowsSyncRemoteSource,
    private val localDataSource: ProgressWatchingLocalSource,
) {
    suspend fun getLocalWatching(limit: Int): ImmutableList<ProfileProgressItem> {
        return localDataSource.getItems()
            .filterIsInstance<ProfileProgressItem.ShowItem>()
            .sortedWith(compareBy({ it.remainingEpisodes }, { it.show.title }))
            .take(limit)
            .toImmutableList()
    }

    suspend fun getWatching(
        page: Int,
        limit: Int,
    ): ImmutableList<ProfileProgressItem> {
        val remoteItems = remoteShowsSyncSource.getUpNext(
            limit = limit,
            page = page,
            intent = "continue",
            sortBy = "remaining",
            sortHow = "asc",
            filters = null,
        )

        return remoteItems
            .asyncMap {
                ProfileProgressItem.ShowItem(
                    show = Show.fromDto(it.show),
                    remainingEpisodes = (it.progress.aired - it.progress.completed)
                        .coerceAtLeast(0),
                )
            }
            .sortedWith(compareBy({ it.remainingEpisodes }, { it.show.title }))
            .also {
                when (page) {
                    1 -> localDataSource.setItems(it)
                    else -> localDataSource.addItems(it)
                }
            }
            .toImmutableList()
    }
}
