package tv.trakt.trakt.common.data.remote

import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.networking.LikedListDto
import tv.trakt.trakt.common.networking.ListDto

class UserOtherListsRemoteSource(
    private val usersApi: UsersApi,
) {
    suspend fun getLikedLists(
        minimal: Boolean = false,
        pagination: Pagination,
    ): List<LikedListDto> {
        val response = usersApi.getUsersLikes(
            id = "me",
            type = "lists",
            extended = when {
                minimal -> "min"
                else -> "images"
            },
            page = pagination.page,
            limit = pagination.limit.toString(),
        )
        return response.body()
    }

    suspend fun getCollaborationLists(userId: String = "me"): List<ListDto> {
        val response = usersApi.getUsersListsCollaborations(
            id = userId,
            extended = "images",
        )
        return response.body()
    }
}
