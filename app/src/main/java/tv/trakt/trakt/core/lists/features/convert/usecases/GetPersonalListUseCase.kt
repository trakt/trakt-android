package tv.trakt.trakt.core.lists.features.convert.usecases

import tv.trakt.trakt.common.data.remote.ListsRemoteSource
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomList

internal class GetPersonalListUseCase(
    private val remoteSource: ListsRemoteSource,
) {
    suspend fun getList(listId: TraktId): CustomList {
        return CustomList.fromDto(remoteSource.getList(listId))
    }
}
