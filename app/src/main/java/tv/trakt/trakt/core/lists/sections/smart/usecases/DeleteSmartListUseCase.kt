package tv.trakt.trakt.core.lists.sections.smart.usecases

import tv.trakt.trakt.common.data.remote.UserSmartListsRemoteSource
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.sections.smart.data.local.ListsSmartLocalDataSource

internal class DeleteSmartListUseCase(
    private val remoteSource: UserSmartListsRemoteSource,
    private val localSource: ListsSmartLocalDataSource,
) {
    suspend fun deleteList(listId: TraktId) {
        remoteSource.deleteSmartList(listId = listId)
        with(localSource) {
            removeList(listId)
            notifyUpdate()
        }
    }
}
