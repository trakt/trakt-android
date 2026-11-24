package tv.trakt.trakt.core.lists.features.convert.usecases

import tv.trakt.trakt.common.data.remote.UserSmartListsRemoteSource
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.SmartListDraft

internal class GetSmartListDraftUseCase(
    private val remoteSource: UserSmartListsRemoteSource,
) {
    suspend fun getDraft(listId: TraktId): SmartListDraft? {
        return remoteSource.getSmartListDraft(listId)?.let(SmartListDraft::fromDto)
    }
}
