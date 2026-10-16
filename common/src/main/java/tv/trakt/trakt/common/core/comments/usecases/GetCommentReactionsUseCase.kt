package tv.trakt.trakt.common.core.comments.usecases

import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.core.comments.data.remote.CommentsRemoteSource
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary

private val supportedReactions = Reaction.entries
    .map { it.name.uppercase() }

class GetCommentReactionsUseCase(
    private val remoteSource: CommentsRemoteSource,
) {
    suspend fun getReactions(commentId: Int): ReactionsSummary {
        val remoteReactions = remoteSource.getCommentReactions(commentId)
        return ReactionsSummary(
            reactionsCount = remoteReactions.reactionCount,
            distribution = remoteReactions.distribution
                .filterKeys { supportedReactions.contains(it.uppercase()) }
                .mapKeys { Reaction.valueOf(it.key.uppercase()) }
                .toImmutableMap(),
        )
    }
}
