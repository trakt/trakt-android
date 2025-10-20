package tv.trakt.trakt.common.core.comments.data.remote

import org.openapitools.client.models.GetCommentsReactionsSummary200Response
import tv.trakt.trakt.common.networking.CommentDto

interface CommentsRemoteDataSource {
    suspend fun getCommentReplies(commentId: Int): List<CommentDto>

    suspend fun getCommentReactions(commentId: Int): GetCommentsReactionsSummary200Response
}
