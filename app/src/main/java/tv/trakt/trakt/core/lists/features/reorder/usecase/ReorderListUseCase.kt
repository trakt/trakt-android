package tv.trakt.trakt.core.lists.features.reorder.usecase

import tv.trakt.trakt.common.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.common.model.TraktId

internal class ReorderListUseCase(
    private val remoteSource: ListsRemoteDataSource,
) {
    suspend fun reorderList(
        listId: TraktId,
        itemIds: List<Int>,
    ) {
        remoteSource.reorderListItems(listId, itemIds)
    }
}
