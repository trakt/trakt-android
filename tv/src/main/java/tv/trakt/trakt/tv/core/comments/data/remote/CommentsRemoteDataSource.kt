package tv.trakt.trakt.tv.core.comments.data.remote

import tv.trakt.trakt.common.networking.CommentDto

internal interface CommentsRemoteDataSource {
    suspend fun getCommentReplies(commentId: Int): List<CommentDto>
}
