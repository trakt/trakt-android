package tv.trakt.trakt.core.lists.sheets.edit.usecases

import tv.trakt.trakt.common.data.local.ListsPersonalLocalSource
import tv.trakt.trakt.common.data.remote.ListsRemoteSource
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.sorting.Sorting

internal class EditListUseCase(
    private val remoteSource: ListsRemoteSource,
    private val localSource: ListsPersonalLocalSource,
) {
    suspend fun editList(
        listId: TraktId,
        name: String,
        description: String?,
        privacy: CustomList.Privacy,
        sorting: Sorting?,
    ) {
        remoteSource.editList(
            listId = listId,
            name = name.trim(),
            description = description?.trim(),
            privacy = privacy.value,
            sorting = sorting,
        )
        localSource.editItem(
            listId = listId,
            name = name.trim(),
            description = description?.trim(),
            privacy = privacy,
            sorting = sorting,
            notify = true,
        )
    }

    suspend fun deleteList(listId: TraktId) {
        remoteSource.deleteList(
            listId = listId,
        )
        localSource.deleteItem(
            id = listId,
            notify = true,
        )
    }
}
