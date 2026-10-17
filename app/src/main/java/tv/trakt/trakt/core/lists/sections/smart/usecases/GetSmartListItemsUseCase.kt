package tv.trakt.trakt.core.lists.sections.smart.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.remote.UserSmartListsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.lists.model.SmartListItem

internal class GetSmartListItemsUseCase(
    private val remoteSource: UserSmartListsRemoteSource,
) {
    suspend fun getItems(
        listId: TraktId,
        pagination: Pagination = Pagination.Default,
    ): ImmutableList<SmartListItem> {
        return remoteSource.getSmartListItems(
            listId = listId,
            pagination = pagination,
        ).asyncMap(SmartListItem::fromDto)
            .filterNotNull()
            .toImmutableList()
    }
}
