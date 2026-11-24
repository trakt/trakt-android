package tv.trakt.trakt.core.lists.features.convert.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.remote.UserSmartListsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.lists.model.SmartListItem
import tv.trakt.trakt.core.lists.usecases.LocalizeListItemsUseCase

internal class GetSmartListDraftItemsUseCase(
    private val remoteSource: UserSmartListsRemoteSource,
    private val localizeListItemsUseCase: LocalizeListItemsUseCase,
) {
    suspend fun getItems(
        listId: TraktId,
        pagination: Pagination,
    ): ImmutableList<SmartListItem> {
        return remoteSource.getSmartListDraftItems(
            listId = listId,
            pagination = pagination,
        ).asyncMap(SmartListItem::fromDto)
            .filterNotNull()
            .let { localizeListItemsUseCase.localizeSmart(it) }
            .toImmutableList()
    }
}
