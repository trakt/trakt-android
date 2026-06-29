package tv.trakt.trakt.core.lists.sections.liked.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.MediaMode.MEDIA
import tv.trakt.trakt.common.model.MediaMode.MOVIES
import tv.trakt.trakt.common.model.MediaMode.SHOWS
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.features.details.usecases.GetListItemsUseCase
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.core.lists.sections.liked.data.local.items.ListsLikedItemsLocalDataSource

internal class GetLikedListItemsUseCase(
    private val getListItemsUseCase: GetListItemsUseCase,
    private val localSource: ListsLikedItemsLocalDataSource,
) {
    suspend fun getItems(
        listId: TraktId,
        limit: Int,
        filter: MediaMode,
        sorting: Sorting,
    ): ImmutableList<CustomListItem> {
        return getListItemsUseCase.getItems(
            listId = listId,
            type = when (filter) {
                MEDIA -> listOf(MediaType.Movie, MediaType.Show)
                SHOWS -> listOf(MediaType.Show)
                MOVIES -> listOf(MediaType.Movie)
            },
            sorting = sorting,
            pagination = Pagination(page = 1, limit = limit),
            filters = null,
        ).distinctBy {
            it.key
        }.also {
            localSource.setItems(
                listId = listId,
                items = it,
            )
        }.toImmutableList()
    }

    suspend fun getLocalItems(
        listId: TraktId,
        filter: MediaMode,
    ): ImmutableList<CustomListItem> {
        return localSource.getItems(listId)
            .filter {
                when (filter) {
                    MEDIA -> true
                    SHOWS -> it is CustomListItem.ShowItem
                    MOVIES -> it is CustomListItem.MovieItem
                }
            }
            .distinctBy { it.key }
            .toImmutableList()
    }
}
