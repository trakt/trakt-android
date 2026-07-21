package tv.trakt.trakt.app.core.home.sections.startwatching.usecases

import tv.trakt.trakt.app.core.home.sections.startwatching.model.WatchlistItem
import tv.trakt.trakt.app.core.sync.data.remote.shows.ShowsSyncRemoteDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.SortOrder
import tv.trakt.trakt.common.model.sorting.SortType
import tv.trakt.trakt.common.model.sorting.Sorting

private val SortComparator =
    compareByDescending<WatchlistItem> { it.released }
        .thenByDescending { it.listedAt }

internal class GetHomeShowsWatchlistItemsUseCase(
    private val remoteSyncSource: ShowsSyncRemoteDataSource,
    private val localShowsSource: ShowLocalDataSource,
) {
    suspend fun getItems(pagination: Pagination): List<WatchlistItem> {
        val nowDay = nowLocal().toString()

        val response = remoteSyncSource.getWatchlist(
            sorting = Sorting(
                type = SortType.Released,
                order = SortOrder.Desc,
            ),
            page = pagination.page,
            limit = pagination.limit,
            extended = "full,cloud9,colors,streaming_ids",
            hide = "unreleased",
        ).filter {
            !it.show.firstAired.isNullOrBlank() && it.show.firstAired!! <= nowDay
        }.asyncMap { item ->
            WatchlistItem.ShowItem(
                show = Show.fromDto(item.show),
                rank = item.rank,
                listedAt = item.listedAt.toZonedDateTime(),
            )
        }.sortedWith(SortComparator)

        return response
            .also {
                val shows = response.asyncMap { it.show }
                localShowsSource.upsertShows(shows)
            }
    }
}
