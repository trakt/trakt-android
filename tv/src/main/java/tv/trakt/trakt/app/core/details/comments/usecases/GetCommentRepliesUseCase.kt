package tv.trakt.trakt.app.core.details.comments.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.common.model.Comment
import tv.trakt.trakt.app.core.comments.data.remote.CommentsRemoteDataSource

internal class GetCommentRepliesUseCase(
    private val remoteSource: CommentsRemoteDataSource,
) {
    suspend fun getCommentReplies(commentId: Int): ImmutableList<Comment> {
        val remoteComments = remoteSource.getCommentReplies(commentId)
            .filter { it.parentId > 0 }
            .sortedBy { it.createdAt }
            .map { Comment.fromDto(it) }

        return remoteComments.toImmutableList()
    }
}
