package tv.trakt.trakt.core.lists.features.reorder.watchlist.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.remote.UserWatchlistRemoteSource
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.sorting.SortOrder
import tv.trakt.trakt.common.model.sorting.SortType
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.WatchlistItemDto
import tv.trakt.trakt.core.lists.ListsConfig.LISTS_ITEMS_ALL_LIMIT
import tv.trakt.trakt.core.lists.features.reorder.watchlist.WatchlistReorderEntry

internal class ReorderWatchlistUseCase(
    private val remoteSource: UserWatchlistRemoteSource,
) {
    suspend fun getRankedItems(): ImmutableList<WatchlistReorderEntry> {
        val all = mutableListOf<WatchlistItemDto>()
        var page = 1

        while (true) {
            val pageItems = remoteSource.getWatchlist(
                page = page,
                limit = LISTS_ITEMS_ALL_LIMIT,
                extended = "full,images",
                sorting = RankSorting,
                filters = null,
            )

            all += pageItems

            if (pageItems.size < LISTS_ITEMS_ALL_LIMIT) break
            page += 1
        }

        return all
            .mapNotNull(::toEntry)
            .distinctBy { it.key }
            .toImmutableList()
    }

    suspend fun reorderWatchlist(itemIds: List<Int>) {
        remoteSource.reorderWatchlist(itemIds)
    }
}

private val RankSorting = Sorting(
    type = SortType.Default,
    order = SortOrder.Asc,
)

private fun toEntry(dto: WatchlistItemDto): WatchlistReorderEntry? {
    dto.movie?.let {
        val movie = Movie.fromDto(it)
        return WatchlistReorderEntry(
            id = dto.id,
            key = "${movie.ids.trakt.value}-movie",
            title = movie.title,
            year = movie.year,
            images = movie.images,
        )
    }

    dto.show?.let {
        val show = Show.fromDto(it)
        return WatchlistReorderEntry(
            id = dto.id,
            key = "${show.ids.trakt.value}-show",
            title = show.title,
            year = show.year,
            images = show.images,
        )
    }

    return null
}
