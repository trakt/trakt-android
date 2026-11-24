package tv.trakt.trakt.core.lists.features.convert.usecases

import tv.trakt.trakt.common.data.remote.UserSmartListsRemoteSource
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.SmartList

internal class GetConvertedSmartListUseCase(
    private val remoteSource: UserSmartListsRemoteSource,
) {
    suspend fun getSmartList(smartListId: TraktId): SmartList {
        return SmartList.fromDto(remoteSource.getSmartListDetails(smartListId))
    }
}
