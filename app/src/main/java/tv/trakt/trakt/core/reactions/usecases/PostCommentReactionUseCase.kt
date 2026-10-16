package tv.trakt.trakt.core.reactions.usecases

import tv.trakt.trakt.common.core.reactions.data.remote.ReactionsRemoteSource
import tv.trakt.trakt.common.model.reactions.Reaction

internal class PostCommentReactionUseCase(
    private val remoteSource: ReactionsRemoteSource,
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
