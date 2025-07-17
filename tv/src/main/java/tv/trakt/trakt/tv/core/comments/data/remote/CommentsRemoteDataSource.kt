package tv.trakt.trakt.tv.core.comments.data.remote

import tv.trakt.trakt.tv.networking.openapi.CommentDto

internal interface CommentsRemoteDataSource {
    suspend fun getCommentReplies(commentId: Int): List<CommentDto>
}
