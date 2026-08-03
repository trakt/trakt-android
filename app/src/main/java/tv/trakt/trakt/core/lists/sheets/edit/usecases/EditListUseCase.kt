package tv.trakt.trakt.core.lists.sheets.edit.usecases

import tv.trakt.trakt.common.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalLocalDataSource

internal class EditListUseCase(
    private val remoteSource: ListsRemoteDataSource,
    private val localSource: ListsPersonalLocalDataSource,
) {
    suspend fun editList(
        listId: TraktId,
        name: String,
        description: String?,
        privacy: CustomList.Privacy,
    ) {
        remoteSource.editList(
            listId = listId,
            name = name.trim(),
            description = description?.trim(),
            privacy = privacy.value,
        )
        localSource.editItem(
            listId = listId,
            name = name.trim(),
            description = description?.trim(),
            privacy = privacy,
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
