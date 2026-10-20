package tv.trakt.trakt.core.reactions.media.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.local.UserMediaReactionsLocalSource
import tv.trakt.trakt.common.data.remote.MediaReactionsRemoteSource
import tv.trakt.trakt.common.model.reactions.MediaReaction
import tv.trakt.trakt.common.model.reactions.MediaReactionsTarget
import tv.trakt.trakt.common.model.reactions.UserMediaReaction

internal class PostMediaReactionUseCase(
    private val remoteSource: MediaReactionsRemoteSource,
    private val localSource: UserMediaReactionsLocalSource,
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
