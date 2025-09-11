package tv.trakt.trakt.core.lists.data.remote

import org.openapitools.client.apis.ListsApi
import org.openapitools.client.models.PostUsersListsCreateRequest
import org.openapitools.client.models.PutUsersListsListUpdateRequest
import tv.trakt.trakt.common.model.TraktId

internal class ListsApiClient(
    private val listsApi: ListsApi,
) : ListsRemoteDataSource {
    override suspend fun createList(
        name: String,
        description: String?,
    ) {
        val request = PostUsersListsCreateRequest(
            name = name,
            description = description,
        )

        listsApi.postUsersListsCreate(
            id = "me",
            postUsersListsCreateRequest = request,
        )
    }

    override suspend fun editList(
        listId: TraktId,
        name: String,
        description: String?,
    ) {
        val request = PutUsersListsListUpdateRequest(
            name = name,
            description = description,
        )

        listsApi.putUsersListsListUpdate(
            id = "me",
            listId = listId.value.toString(),
            putUsersListsListUpdateRequest = request,
        )
    }

    override suspend fun deleteList(listId: TraktId) {
        listsApi.deleteUsersListsListDelete(
            id = "me",
            listId = listId.value.toString(),
        )
    }
}
