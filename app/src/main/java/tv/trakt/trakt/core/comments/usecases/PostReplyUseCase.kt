package tv.trakt.trakt.core.comments.usecases

import tv.trakt.trakt.common.core.comments.data.remote.CommentsRemoteDataSource
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.CommentGif
import tv.trakt.trakt.common.model.TraktId

internal class PostReplyUseCase(
    private val remoteSource: CommentsRemoteDataSource,
) {
    suspend fun postReply(
        commentId: TraktId,
        text: String,
        spoiler: Boolean,
        gif: CommentGif? = null,
    ): Comment {
        val responseComment = remoteSource.postReply(
            commentId = commentId,
            text = text,
            spoiler = spoiler,
            gif = gif,
        )
        return Comment.fromDto(responseComment)
    }
}
