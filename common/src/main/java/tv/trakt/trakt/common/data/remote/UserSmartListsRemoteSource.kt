package tv.trakt.trakt.common.data.remote

import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import org.openapitools.client.apis.ListsApi
import org.openapitools.client.apis.SmartListDraftsApi
import org.openapitools.client.apis.SmartListsApi
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.common.model.lists.toApiParams
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.networking.CreateSmartListRequestDto
import tv.trakt.trakt.common.networking.ListIdsDto
import tv.trakt.trakt.common.networking.SmartListDraftDto
import tv.trakt.trakt.common.networking.SmartListDraftsSummaryDto
import tv.trakt.trakt.common.networking.SmartListDto
import tv.trakt.trakt.common.networking.SmartListItemDto
import tv.trakt.trakt.common.networking.SmartListTemplateDto
import tv.trakt.trakt.common.networking.UpdateSmartListRequestDto
import tv.trakt.trakt.common.networking.api.v3.V3Api

class UserSmartListsRemoteSource(
    private val smartListsApi: SmartListsApi,
    private val smartListDraftsApi: SmartListDraftsApi,
    private val listsApi: ListsApi,
    private val v3Api: V3Api,
) {
    suspend fun getSmartLists(userId: String = "me"): List<SmartListDto> {
        return smartListsApi.getUsersSmartListsPersonal(id = userId).body()
    }

    suspend fun createSmartList(
        request: CreateSmartListRequestDto,
        userId: String = "me",
    ) {
        smartListsApi.postUsersSmartListsCreate(
            id = userId,
            postUsersListsListConvertToSmartListRequest = request,
        )
    }

    suspend fun updateSmartList(
        listId: TraktId,
        request: UpdateSmartListRequestDto,
        userId: String = "me",
    ) {
        val response = smartListsApi.putUsersSmartListsSmartListUpdate(
            id = userId,
            listId = listId.value.toString(),
            putUsersSmartListsSmartListUpdateRequest = request,
        )
        check(response.success) { "Smart list update failed with status ${response.status}" }
    }

    suspend fun getSmartListDetails(
        listId: TraktId,
        userId: String = "me",
    ): SmartListDto {
        return smartListsApi.getUsersSmartListsSmartListSummary(
            id = userId,
            listId = listId.value.toString(),
        ).body()
    }

    suspend fun getSmartListItems(
        listId: TraktId,
        pagination: Pagination = Pagination(),
        extended: String = "full,colors",
    ): List<SmartListItemDto> {
        return smartListsApi.getSmartListsItems(
            listId = listId.value.toString(),
            extended = extended,
            watchnow = null,
            genres = null,
            subgenres = null,
            years = null,
            ratings = null,
            runtimes = null,
            countries = null,
            certifications = null,
            ignoreWatched = null,
            ignoreWatchlisted = null,
            page = pagination.page,
            limit = pagination.limit.toString(),
        ).body()
    }

    suspend fun getSmartListPreviewItems(
        filters: SmartListFilters,
        pagination: Pagination = Pagination(),
        extended: String = "full,colors",
    ): List<SmartListItemDto> {
        return v3Api.getSmartListPreviewItems(
            params = filters.toPreviewParams() + ("extended" to extended),
            pagination = pagination,
        )
    }

    suspend fun getSmartListDraft(
        listId: TraktId,
        userId: String = "me",
    ): SmartListDraftDto? {
        return try {
            listsApi.getUsersListsListSmartListDraft(
                id = userId,
                listId = listId.value.toString(),
            ).body()
        } catch (error: ClientRequestException) {
            if (error.response.status != HttpStatusCode.NotFound) throw error
            null
        }
    }

    suspend fun getSmartListDraftItems(
        listId: TraktId,
        pagination: Pagination = Pagination(),
        extended: String = "full,colors",
    ): List<SmartListItemDto> {
        return v3Api.getSmartListDraftItems(
            listId = listId,
            extended = extended,
            pagination = pagination,
        )
    }

    suspend fun convertToSmartList(
        listId: TraktId,
        request: CreateSmartListRequestDto,
        userId: String = "me",
    ): ListIdsDto {
        return listsApi.postUsersListsListConvertToSmartList(
            id = userId,
            listId = listId.value.toString(),
            postUsersListsListConvertToSmartListRequest = request,
        ).body().ids
    }

    suspend fun getSmartListDraftsSummary(userId: String = "me"): SmartListDraftsSummaryDto {
        return smartListDraftsApi.getUsersSmartListDraftsSummary(id = userId).body()
    }

    suspend fun getSmartListTemplates(): List<SmartListTemplateDto> {
        return smartListsApi.getSmartListsTemplates().body()
    }

    suspend fun deleteSmartList(
        listId: TraktId,
        userId: String = "me",
    ) {
        smartListsApi.deleteUsersSmartListsSmartListDelete(
            id = userId,
            listId = listId.value.toString(),
        )
    }
}

private fun SmartListFilters.toPreviewParams(): Map<String, String> = withoutShowOnlyFilters().toApiParams()
