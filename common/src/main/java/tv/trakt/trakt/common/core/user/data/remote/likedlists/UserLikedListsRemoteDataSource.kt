package tv.trakt.trakt.common.core.user.data.remote.likedlists

import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.networking.LikedListDto
import tv.trakt.trakt.common.networking.ListDto

interface UserLikedListsRemoteDataSource {
    suspend fun getLikedLists(
        minimal: Boolean = false,
        pagination: Pagination,
    ): List<LikedListDto>

    suspend fun getCollaborationLists(): List<ListDto>
}
