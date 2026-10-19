package tv.trakt.trakt.core.reactions.media.usecases

import tv.trakt.trakt.common.data.local.UserMediaReactionsLocalSource
import tv.trakt.trakt.common.data.remote.MediaReactionsRemoteSource
import tv.trakt.trakt.common.model.reactions.MediaReaction
import tv.trakt.trakt.common.model.reactions.MediaReactionsTarget

internal class DeleteMediaReactionUseCase(
    private val remoteSource: MediaReactionsRemoteSource,
    private val localSource: UserMediaReactionsLocalSource,
    private val loadUserReactionsUseCase: LoadUserMediaReactionsUseCase,
) {
    suspend fun deleteReactions(
        target: MediaReactionsTarget,
        reactions: Set<MediaReaction>,
    ) {
        // The API removes by row id, so the held rows must be known first.
        val held = when {
            localSource.isLoaded(target) -> localSource.getReactions(target)
            else -> loadUserReactionsUseCase.loadReactions(target)
        }

        val (removed, kept) = held.partition { it.reaction in reactions }
        if (removed.isEmpty()) return

        remoteSource.deleteUserReactions(
            target = target,
            reactionIds = removed.map { it.id },
        )

        localSource.setReactions(
            target = target,
            reactions = kept,
            notify = true,
        )
    }
}
