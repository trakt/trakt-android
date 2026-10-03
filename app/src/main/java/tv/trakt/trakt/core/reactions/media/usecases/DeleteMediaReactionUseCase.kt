package tv.trakt.trakt.core.reactions.media.usecases

import tv.trakt.trakt.common.core.user.data.local.reactions.UserMediaReactionsLocalDataSource
import tv.trakt.trakt.common.model.reactions.MediaReaction
import tv.trakt.trakt.common.model.reactions.MediaReactionsTarget
import tv.trakt.trakt.core.reactions.media.data.remote.MediaReactionsRemoteDataSource

internal class DeleteMediaReactionUseCase(
    private val remoteSource: MediaReactionsRemoteDataSource,
    private val localSource: UserMediaReactionsLocalDataSource,
    private val loadUserReactionsUseCase: LoadUserMediaReactionsUseCase,
) {
    suspend fun deleteReaction(
        target: MediaReactionsTarget,
        reaction: MediaReaction,
    ) {
        // The API removes by row id, so the held rows must be known first.
        val held = when {
            localSource.isLoaded(target) -> localSource.getReactions(target)
            else -> loadUserReactionsUseCase.loadReactions(target)
        }

        val (removed, kept) = held.partition { it.reaction == reaction }
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
