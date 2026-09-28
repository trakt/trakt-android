package tv.trakt.trakt.core.lists.features.reorder.lists.usecase

import tv.trakt.trakt.common.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.lists.ListsConfig.LISTS_ALL_PAGE_LIMIT
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListsUseCase

internal class ReorderPersonalListsUseCase(
    private val remoteSource: ListsRemoteDataSource,
    private val getPersonalListsUseCase: GetPersonalListsUseCase,
) {
    suspend fun reorderLists(listIds: List<TraktId>) {
        remoteSource.reorderLists(listIds)

        // Refresh the local store in the new order so observing screens reload.
        getPersonalListsUseCase.getLists(
            pagination = Pagination(1, LISTS_ALL_PAGE_LIMIT),
            notify = true,
        )
    }
}
