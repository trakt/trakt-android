package tv.trakt.trakt.core.lists.features.addfrom.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserListsUseCase
import tv.trakt.trakt.common.data.remote.ListsRemoteSource
import tv.trakt.trakt.common.data.remote.UserWatchlistRemoteSource
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.networking.ListMediaItemDto
import tv.trakt.trakt.core.lists.features.addfrom.model.AddFromListsItem
import tv.trakt.trakt.core.lists.features.addfrom.model.AddFromListsSource

private const val LISTED_PAGE_LIMIT = 100

internal class GetAddFromListsUseCase(
    private val loadUserListsUseCase: LoadUserListsUseCase,
    private val listsRemoteSource: ListsRemoteSource,
    private val watchlistRemoteSource: UserWatchlistRemoteSource,
) {
    suspend fun getSources(
        userId: TraktId,
        targetListId: TraktId,
    ): ImmutableList<AddFromListsSource> {
        val lists = loadUserListsUseCase.loadLists().values
            .filter { it.ownerId == userId && it.id != targetListId }
            .map {
                AddFromListsSource.PersonalList(
                    id = it.id,
                    name = it.name,
                    count = it.itemsCount,
                )
            }

        return (listOf(AddFromListsSource.Watchlist) + lists).toImmutableList()
    }

    suspend fun getItems(
        source: AddFromListsSource,
        pagination: Pagination,
    ): List<AddFromListsItem> {
        val items = when (source) {
            is AddFromListsSource.Watchlist -> watchlistRemoteSource.getWatchlist(
                page = pagination.page,
                limit = pagination.limit,
                extended = null,
                sorting = Sorting.Default,
                filters = null,
            )
            is AddFromListsSource.PersonalList -> listsRemoteSource.getMediaListItems(
                listId = source.id,
                extended = null,
                sorting = Sorting.Default,
                pagination = pagination,
                filters = null,
            )
        }

        return items.mapNotNull(::toItem)
    }

    suspend fun getListedKeys(listId: TraktId): ImmutableSet<String> {
        val keys = mutableSetOf<String>()
        var page = 1

        while (true) {
            val pageItems = listsRemoteSource.getMediaListItems(
                listId = listId,
                extended = null,
                sorting = Sorting.Default,
                pagination = Pagination(page, LISTED_PAGE_LIMIT),
                filters = null,
            )

            keys += pageItems.mapNotNull { toItem(it)?.key }

            if (pageItems.size < LISTED_PAGE_LIMIT) break
            page += 1
        }

        return keys.toImmutableSet()
    }

    private fun toItem(dto: ListMediaItemDto): AddFromListsItem? {
        dto.movie?.let {
            val id = it.ids.trakt.toTraktId()
            return AddFromListsItem(
                type = MediaType.Movie,
                id = id,
                title = it.title,
                year = it.year,
                posterUrl = Images.forMovie(id).getPosterUrl(),
            )
        }

        dto.show?.let {
            val id = it.ids.trakt.toTraktId()
            return AddFromListsItem(
                type = MediaType.Show,
                id = id,
                title = it.title,
                year = it.year,
                posterUrl = Images.forShow(id).getPosterUrl(),
            )
        }

        return null
    }
}
