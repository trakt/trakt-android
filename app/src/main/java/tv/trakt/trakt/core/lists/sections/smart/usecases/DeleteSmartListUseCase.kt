package tv.trakt.trakt.core.lists.sections.smart.usecases

import tv.trakt.trakt.common.core.user.data.remote.smartlists.UserSmartListsRemoteDataSource
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.sections.smart.data.local.ListsSmartLocalDataSource

internal class DeleteSmartListUseCase(
    private val remoteSource: UserSmartListsRemoteDataSource,
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
