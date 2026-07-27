package tv.trakt.trakt.core.comments.usecases

import tv.trakt.trakt.common.core.comments.data.remote.CommentsRemoteDataSource

internal class ReportCommentUseCase(
    private val remoteSource: CommentsRemoteDataSource,
) {
    suspend fun reportComment(
        commentId: Int,
        reason: String,
        message: String,
    ) {
        remoteSource.postReport(
            commentId = commentId,
            reason = reason,
            message = message,
        )
    }
}
