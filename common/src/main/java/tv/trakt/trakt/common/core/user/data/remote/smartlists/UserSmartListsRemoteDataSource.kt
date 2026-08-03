package tv.trakt.trakt.common.core.user.data.remote.smartlists

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.CreateSmartListRequestDto
import tv.trakt.trakt.common.networking.SmartListDto
import tv.trakt.trakt.common.networking.SmartListItemDto

interface UserSmartListsRemoteDataSource {
    suspend fun getSmartLists(userId: String = "me"): List<SmartListDto>

    suspend fun createSmartList(
        request: CreateSmartListRequestDto,
        userId: String = "me",
    )

    suspend fun getSmartListDetails(
        listId: TraktId,
        userId: String = "me",
    ): SmartListDto

    suspend fun getSmartListItems(
        listId: TraktId,
        type: String,
        sorting: Sorting = Sorting.Default,
        pagination: Pagination = Pagination(),
        extended: String = "full,images,colors",
    ): List<SmartListItemDto>

    suspend fun deleteSmartList(
        listId: TraktId,
        userId: String = "me",
    )
}
