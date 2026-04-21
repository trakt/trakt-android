package tv.trakt.trakt.core.home.sections.watchlist.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.user.data.remote.watchlist.UserWatchlistRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.sorting.SortOrder
import tv.trakt.trakt.common.model.sorting.SortTypeList
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.WatchlistShowDto
import tv.trakt.trakt.core.home.sections.watchlist.data.local.HomeWatchlistLocalDataSource
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem

internal class GetHomeShowsWatchlistUseCase(
    private val userRemoteSource: UserWatchlistRemoteDataSource,
    private val homeWatchlistLocalSource: HomeWatchlistLocalDataSource,
) {
    suspend fun getLocalWatchlist(limit: Int? = null): ImmutableList<WatchlistItem> {
        return homeWatchlistLocalSource.getShowItems()
            .take(limit ?: Int.MAX_VALUE)
            .toImmutableList()
    }

    suspend fun getWatchlist(limit: Int): ImmutableList<WatchlistItem> {
        return userRemoteSource.getWatchlistShows(
            page = 1,
            limit = limit,
            extended = "full,cloud9,colors",
            sorting = Sorting(
                type = SortTypeList.RELEASED,
                order = SortOrder.DESCENDING,
            ),
            hide = "unreleased",
        ).asyncMap {
            mapShowItem(it)
        }.also {
            homeWatchlistLocalSource.setShowItems(items = it)
        }
            .take(limit)
            .toImmutableList()
    }

    private fun mapShowItem(item: WatchlistShowDto): WatchlistItem.ShowItem {
        return WatchlistItem.ShowItem(
            show = Show.fromDto(item.show),
            rank = item.rank,
            listedAt = item.listedAt.toInstant(),
        )
    }
}
