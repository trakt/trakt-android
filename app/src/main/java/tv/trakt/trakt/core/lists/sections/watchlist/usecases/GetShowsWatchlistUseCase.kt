package tv.trakt.trakt.core.lists.sections.watchlist.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.lists.ListsConfig.LISTS_SECTION_LIMIT
import tv.trakt.trakt.core.lists.sections.watchlist.data.local.ListsWatchlistLocalDataSource
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.sync.data.remote.shows.ShowsSyncRemoteDataSource

internal class GetShowsWatchlistUseCase(
    private val remoteSyncSource: ShowsSyncRemoteDataSource,
    private val localSource: ListsWatchlistLocalDataSource,
) {
    suspend fun getLocalWatchlist(): ImmutableList<WatchlistItem> {
        return localSource.getItems()
            .sortedByDescending { it.listedAt }
            .toImmutableList()
    }

    suspend fun getWatchlist(
        limit: Int = LISTS_SECTION_LIMIT,
        page: Int = 1,
    ): ImmutableList<WatchlistItem> {
        val response = remoteSyncSource.getWatchlist(
            page = page,
            limit = limit,
            sort = "added",
            extended = "full,cloud9",
        ).asyncMap {
            WatchlistItem.ShowItem(
                show = Show.fromDto(it.show),
                listedAt = it.listedAt.toInstant(),
            )
        }

        return response
            .toImmutableList()
            .also {
                localSource.addItems(it)
            }
    }
}
