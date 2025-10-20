package tv.trakt.trakt.core.reactions.usecases

import tv.trakt.trakt.core.reactions.data.remote.ReactionsRemoteDataSource

internal class DeleteCommentReactionUseCase(
    private val remoteSource: ReactionsRemoteDataSource,
) {
    suspend fun deleteReactions(commentId: Int) {
        remoteSource.deleteUserReactions(
            commentId = commentId,
        )
    }
}
