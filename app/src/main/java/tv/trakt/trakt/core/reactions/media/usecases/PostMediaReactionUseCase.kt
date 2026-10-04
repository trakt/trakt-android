package tv.trakt.trakt.core.reactions.media.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.user.data.local.reactions.UserMediaReactionsLocalDataSource
import tv.trakt.trakt.common.model.reactions.MediaReaction
import tv.trakt.trakt.common.model.reactions.MediaReactionsTarget
import tv.trakt.trakt.common.model.reactions.UserMediaReaction
import tv.trakt.trakt.core.reactions.media.data.remote.MediaReactionsRemoteDataSource

internal class PostMediaReactionUseCase(
    private val remoteSource: MediaReactionsRemoteDataSource,
    private val localSource: UserMediaReactionsLocalDataSource,
) {
    suspend fun postReactions(
        target: MediaReactionsTarget,
        reactions: List<MediaReaction>,
    ): ImmutableList<UserMediaReaction> {
        return remoteSource.postUserReactions(
            target = target,
            reactions = reactions.map { it.value },
        )
            .mapNotNull(UserMediaReaction::fromDto)
            .toImmutableList()
            .also {
                localSource.setReactions(
                    target = target,
                    reactions = it,
                    notify = true,
                )
            }
    }
}
