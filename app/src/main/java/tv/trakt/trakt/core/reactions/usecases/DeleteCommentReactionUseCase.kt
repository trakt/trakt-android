package tv.trakt.trakt.core.reactions.usecases

import tv.trakt.trakt.common.data.remote.ReactionsRemoteSource

internal class DeleteCommentReactionUseCase(
    private val remoteSource: ReactionsRemoteSource,
) {
    suspend fun deleteReactions(commentId: Int) {
        remoteSource.deleteUserReactions(
            commentId = commentId,
        )
    }
}
