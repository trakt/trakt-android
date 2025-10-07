package tv.trakt.trakt.common.core.comments.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.comments.data.remote.CommentsRemoteDataSource
import tv.trakt.trakt.common.model.Comment

class GetCommentRepliesUseCase(
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
