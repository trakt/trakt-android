package tv.trakt.trakt.core.applinks.usecases

import tv.trakt.trakt.common.data.remote.CommentsRemoteSource
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.TraktId

internal class GetLinkedCommentUseCase(
    private val remoteSource: CommentsRemoteSource,
) {
    suspend fun getComment(commentId: TraktId): Comment {
        return Comment.fromDto(remoteSource.getComment(commentId))
    }
}
