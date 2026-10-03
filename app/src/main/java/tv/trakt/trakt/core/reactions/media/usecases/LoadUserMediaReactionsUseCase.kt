package tv.trakt.trakt.core.reactions.media.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.user.data.local.reactions.UserMediaReactionsLocalDataSource
import tv.trakt.trakt.common.model.reactions.MediaReactionsTarget
import tv.trakt.trakt.common.model.reactions.UserMediaReaction
import tv.trakt.trakt.core.reactions.media.data.remote.MediaReactionsRemoteDataSource

internal class LoadUserMediaReactionsUseCase(
    private val remoteSource: MediaReactionsRemoteDataSource,
    private val localSource: UserMediaReactionsLocalDataSource,
) {
    suspend fun isLoaded(target: MediaReactionsTarget): Boolean {
        return localSource.isLoaded(target)
    }

    suspend fun loadLocalReactions(target: MediaReactionsTarget): ImmutableList<UserMediaReaction> {
        return localSource.getReactions(target)
            .toImmutableList()
    }

    suspend fun loadReactions(target: MediaReactionsTarget): ImmutableList<UserMediaReaction> {
        return remoteSource.getUserReactions(target)
            .mapNotNull(UserMediaReaction::fromDto)
            .toImmutableList()
            .also {
                localSource.setReactions(target, it)
            }
    }
}
