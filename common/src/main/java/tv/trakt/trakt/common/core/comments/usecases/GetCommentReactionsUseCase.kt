package tv.trakt.trakt.common.core.comments.usecases

import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.core.comments.data.remote.CommentsRemoteDataSource
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary

private val supportedReactions = Reaction.entries
    .map { it.name.uppercase() }

class GetCommentReactionsUseCase(
    private val remoteSource: CommentsRemoteDataSource,
) {
    suspend fun getReactions(commentId: Int): ReactionsSummary {
        val remoteReactions = remoteSource.getCommentReactions(commentId)
        return ReactionsSummary(
            reactionsCount = remoteReactions.reactionCount,
            usersCount = remoteReactions.userCount,
            distribution = remoteReactions.distribution
                .filterKeys { supportedReactions.contains(it.uppercase()) }
                .mapKeys { Reaction.valueOf(it.key.uppercase()) }
                .toImmutableMap(),
        )
    }
}
