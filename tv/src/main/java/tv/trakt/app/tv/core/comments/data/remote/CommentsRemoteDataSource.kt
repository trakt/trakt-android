package tv.trakt.app.tv.core.comments.data.remote

import tv.trakt.app.tv.networking.openapi.CommentDto

internal interface CommentsRemoteDataSource {
    suspend fun getCommentReplies(commentId: Int): List<CommentDto>
}
