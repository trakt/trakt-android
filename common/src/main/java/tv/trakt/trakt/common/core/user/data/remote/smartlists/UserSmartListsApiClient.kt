package tv.trakt.trakt.common.core.user.data.remote.smartlists

import org.openapitools.client.apis.SmartListsApi
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.CreateSmartListRequestDto
import tv.trakt.trakt.common.networking.SmartListDto
import tv.trakt.trakt.common.networking.SmartListItemDto

class UserSmartListsApiClient(
    private val smartListsApi: SmartListsApi,
) : UserSmartListsRemoteDataSource {
    override suspend fun getSmartLists(userId: String): List<SmartListDto> {
        return smartListsApi.getUsersSmartListsPersonal(id = userId).body()
    }

    override suspend fun createSmartList(
        request: CreateSmartListRequestDto,
        userId: String,
    ) {
        smartListsApi.postUsersSmartListsCreate(
            id = userId,
            postUsersSmartListsCreateRequest = request,
        )
    }

    override suspend fun getSmartListDetails(
        listId: TraktId,
        userId: String,
    ): SmartListDto {
        return smartListsApi.getUsersSmartListsSmartListSummary(
            id = userId,
            listId = listId.value.toString(),
        ).body()
    }

    override suspend fun getSmartListItems(
        listId: TraktId,
        type: String,
        sorting: Sorting,
        pagination: Pagination,
        extended: String,
    ): List<SmartListItemDto> {
        return smartListsApi.getSmartListsItems(
            listId = listId.value.toString(),
            type = type,
            sortBy = sorting.type.value,
            sortHow = sorting.order.value,
            extended = extended,
            watchnow = null,
            genres = null,
            subgenres = null,
            years = null,
            ratings = null,
            startDate = null,
            endDate = null,
            runtimes = null,
            countries = null,
            certifications = null,
            ignoreWatched = null,
            ignoreCollected = null,
            ignoreWatchlisted = null,
            page = pagination.page,
            limit = pagination.limit.toString(),
        ).body()
    }

    override suspend fun deleteSmartList(
        listId: TraktId,
        userId: String,
    ) {
        smartListsApi.deleteUsersSmartListsSmartListDelete(
            id = userId,
            listId = listId.value.toString(),
        )
    }
}
