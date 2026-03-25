package tv.trakt.trakt.common.core.user.data.remote.likedlists

import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.networking.LikedListDto

class UserLikedListsApiClient(
    private val usersApi: UsersApi,
) : UserLikedListsRemoteDataSource {
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
}
