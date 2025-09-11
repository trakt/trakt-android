package tv.trakt.trakt.core.lists.sheets.edit.usecases

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.data.remote.ListsRemoteDataSource

internal class EditListUseCase(
    private val remoteSource: ListsRemoteDataSource,
) {
    suspend fun editList(
        listId: TraktId,
        name: String,
        description: String?,
    ) {
        remoteSource.editList(
            listId = listId,
            name = name.trim(),
            description = description?.trim(),
        )
    }

    suspend fun deleteList(listId: TraktId) {
        remoteSource.deleteList(
            listId = listId,
        )
    }
}
