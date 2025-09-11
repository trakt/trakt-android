package tv.trakt.trakt.core.lists.data.remote

import org.openapitools.client.apis.ListsApi
import org.openapitools.client.models.PostUsersListsCreateRequest

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
}
