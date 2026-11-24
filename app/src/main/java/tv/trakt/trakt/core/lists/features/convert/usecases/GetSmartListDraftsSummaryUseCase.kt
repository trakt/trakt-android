package tv.trakt.trakt.core.lists.features.convert.usecases

import tv.trakt.trakt.common.data.remote.UserSmartListsRemoteSource
import tv.trakt.trakt.common.model.lists.SmartListDraftsSummary

internal class GetSmartListDraftsSummaryUseCase(
    private val remoteSource: UserSmartListsRemoteSource,
) {
    suspend fun getSummary(): SmartListDraftsSummary {
        return SmartListDraftsSummary.fromDto(remoteSource.getSmartListDraftsSummary())
    }
}
