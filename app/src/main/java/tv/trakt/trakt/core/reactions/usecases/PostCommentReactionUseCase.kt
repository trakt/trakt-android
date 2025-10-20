package tv.trakt.trakt.core.reactions.usecases

import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.core.reactions.data.remote.ReactionsRemoteDataSource

internal class PostCommentReactionUseCase(
    private val remoteSource: ReactionsRemoteDataSource,
) {
    suspend fun postReaction(
        commentId: Int,
        reaction: Reaction,
    ) {
        remoteSource.postUserReaction(
            commentId = commentId,
            reaction = reaction.value,
        )
    }
}
