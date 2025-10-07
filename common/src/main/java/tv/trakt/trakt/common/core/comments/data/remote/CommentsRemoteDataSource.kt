package tv.trakt.trakt.common.core.comments.data.remote

import tv.trakt.trakt.common.networking.CommentDto

interface CommentsRemoteDataSource {
    suspend fun getCommentReplies(commentId: Int): List<CommentDto>
}
