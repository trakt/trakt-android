package tv.trakt.trakt.core.lists.sections.watchlist.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.user.data.remote.UserRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.lists.sections.watchlist.model.getWatchlistSorting
import tv.trakt.trakt.core.user.data.local.watchlist.UserWatchlistLocalDataSource

internal class GetShowsWatchlistUseCase(
    private val remoteSource: UserRemoteDataSource,
    private val userWatchlistLocalDataSource: UserWatchlistLocalDataSource,
) {
    suspend fun getLocalWatchlist(
        limit: Int? = null,
        sort: Sorting? = null,
    ): ImmutableList<WatchlistItem> {
        return userWatchlistLocalDataSource.getShows()
            .sortedWith(getWatchlistSorting(sort))
            .take(limit ?: Int.MAX_VALUE)
            .toImmutableList()
    }

    suspend fun getRemoteWatchlist(
        page: Int,
        limit: Int,
        sorting: Sorting,
        skipLocal: Boolean = false,
    ): ImmutableList<WatchlistItem> {
        val response = remoteSource.getWatchlistShows(
            page = page,
            limit = limit,
            sorting = sorting,
            extended = "full,cloud9,colors",
        ).asyncMap {
            val listedAt = it.listedAt.toInstant()
            WatchlistItem.ShowItem(
                show = Show.fromDto(it.show),
                rank = it.rank,
                listedAt = listedAt,
            )
        }

        return response
            .toImmutableList()
            .also {
                if (skipLocal) return@also
                with(userWatchlistLocalDataSource) {
                    if (page == 1) {
                        setShows(response)
                    } else {
                        addShows(response)
                    }
                }
            }
    }
}
