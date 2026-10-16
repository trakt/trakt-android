package tv.trakt.trakt.core.comments.usecases

import tv.trakt.trakt.common.core.comments.data.remote.CommentsRemoteSource
import tv.trakt.trakt.common.model.TraktId

internal class DeleteCommentUseCase(
    private val remoteSource: CommentsRemoteSource,
) {
    suspend fun deleteComment(commentId: TraktId) {
        remoteSource.deleteComment(
            commentId = commentId,
        )
    }
}
