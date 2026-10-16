package tv.trakt.trakt.core.lists.features.smart.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.user.data.remote.smartlists.UserSmartListsRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.lists.model.SmartListItem

private const val MAX_PREVIEW_ITEMS = 100

internal class GetSmartListPreviewUseCase(
    private val remoteSource: UserSmartListsRemoteDataSource,
) {
    suspend fun getPreviewItems(filters: SmartListFilters): ImmutableList<SmartListItem> {
        return remoteSource.getSmartListPreviewItems(
            filters = filters,
            pagination = Pagination(limit = MAX_PREVIEW_ITEMS),
        ).asyncMap(SmartListItem::fromDto)
            .filterNotNull()
            .toImmutableList()
    }
}
