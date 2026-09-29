package tv.trakt.trakt.core.comments.usecases

import tv.trakt.trakt.common.core.comments.data.remote.CommentsRemoteDataSource
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.CommentGif
import tv.trakt.trakt.common.model.toTraktId

internal class EditCommentUseCase(
    private val remoteSource: CommentsRemoteDataSource,
) {
    suspend fun editComment(
        comment: Comment,
        text: String,
        spoiler: Boolean,
        gif: CommentGif?,
    ): Comment {
        val result = remoteSource.editComment(
            commentId = comment.id.toTraktId(),
            text = text,
            spoiler = spoiler,
            gif = gif,
        ).let {
            Comment.fromDto(it)
        }

        // The update response may omit extended data (such as user images) and live counters,
        // so only the edited fields are taken from it.
        return comment.copy(
            comment = result.comment,
            isSpoiler = result.isSpoiler,
            isReview = result.isReview,
            gif = result.gif,
            language = result.language ?: comment.language,
            updatedAt = result.updatedAt,
        )
    }
}
