package tv.trakt.trakt.common.core.comments.data.remote

import org.openapitools.client.apis.CommentsApi
import tv.trakt.trakt.common.networking.CommentDto

class CommentsApiClient(
    private val api: CommentsApi,
) : CommentsRemoteDataSource {
    override suspend fun getCommentReplies(commentId: Int): List<CommentDto> {
        val response = api.getCommentsReplies(
            id = commentId.toString(),
            page = null,
            limit = 99,
        )

        return response.body()
    }
}
