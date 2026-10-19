package tv.trakt.trakt.common.data.remote

import org.openapitools.client.apis.ReactionsApi
import tv.trakt.trakt.common.networking.UserReactionDto
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider

class ReactionsRemoteSource(
    private val reactionsApi: ReactionsApi,
    private val cacheMarker: CacheMarkerProvider,
) {
    suspend fun getUserReactions(): List<UserReactionDto> {
        val response = reactionsApi.getUsersReactionsComments(
            extended = "min",
            page = null,
            limit = "all",
        )

        return response.body()
    }

    suspend fun postUserReaction(
        commentId: Int,
        reaction: String,
    ) {
        with(reactionsApi) {
            deleteCommentsReactionsRemove(
                id = commentId.toString(),
                reactionType = "",
            )
            postCommentsReactionsAdd(
                id = commentId.toString(),
                reactionType = reaction,
                body = null,
            )
        }
        cacheMarker.invalidate()
    }

    suspend fun deleteUserReactions(commentId: Int) {
        reactionsApi.deleteCommentsReactionsRemove(
            id = commentId.toString(),
            reactionType = "",
        )
        cacheMarker.invalidate()
    }
}
