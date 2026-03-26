package tv.trakt.trakt.common.core.user.data.remote.otherlists

import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.networking.LikedListDto
import tv.trakt.trakt.common.networking.ListDto

class UserOtherListsApiClient(
    private val usersApi: UsersApi,
) : UserOtherListsRemoteDataSource {
    override suspend fun getLikedLists(
        minimal: Boolean,
        pagination: Pagination,
    ): List<LikedListDto> {
        val response = usersApi.getUsersLikesLists(
            extended = when {
                minimal -> "min"
                else -> "cloud9,images"
            },
            page = pagination.page,
            limit = pagination.limit.toString(),
        )
        return response.body()
    }

    override suspend fun getCollaborationLists(): List<ListDto> {
        val response = usersApi.getUsersListsCollaborations(
            id = "me",
            extended = "cloud9,images",
        )
        return emptyList()
    }
}
