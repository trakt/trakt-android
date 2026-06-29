package tv.trakt.trakt.core.lists.sections.collaborations.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.MediaMode.Media
import tv.trakt.trakt.common.model.MediaMode.Movies
import tv.trakt.trakt.common.model.MediaMode.Shows
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.features.details.usecases.GetListItemsUseCase
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.core.lists.sections.collaborations.data.local.items.ListsCollaborationsItemsLocalDataSource

internal class GetCollaborationsListItemsUseCase(
    private val getListItemsUseCase: GetListItemsUseCase,
    private val localSource: ListsCollaborationsItemsLocalDataSource,
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
                Media -> listOf(MediaType.Movie, MediaType.Show)
                Shows -> listOf(MediaType.Show)
                Movies -> listOf(MediaType.Movie)
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
                    Media -> true
                    Shows -> it is CustomListItem.ShowItem
                    Movies -> it is CustomListItem.MovieItem
                }
            }
            .distinctBy { it.key }
            .toImmutableList()
    }
}
