package tv.trakt.trakt.core.user.usecases.reactions

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.core.reactions.data.remote.ReactionsRemoteDataSource
import tv.trakt.trakt.core.user.data.local.reactions.UserReactionsLocalDataSource

private val supportedReactions = Reaction.entries
    .map { it.name.uppercase() }

internal class LoadUserReactionsUseCase(
    private val remoteSource: ReactionsRemoteDataSource,
    private val localSource: UserReactionsLocalDataSource,
) {
    suspend fun isLoaded(): Boolean {
        return localSource.isLoaded()
    }

    suspend fun loadLocalReactions(): ImmutableMap<Int, Reaction?> {
        return localSource.getReactions()
            .toImmutableMap()
    }

    suspend fun loadReactions(): ImmutableMap<Int, Reaction?> {
        return remoteSource.getUserReactions()
            .associateBy(
                keySelector = { it.comment.id },
                valueTransform = {
                    val reactionType = it.reaction.type.value.uppercase()
                    if (supportedReactions.contains(reactionType)) {
                        Reaction.valueOf(reactionType)
                    } else {
                        null
                    }
                },
            )
            .filterValues { it != null }
            .toImmutableMap()
            .also {
                localSource.setReactions(it)
            }
    }
}
