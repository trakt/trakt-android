package tv.trakt.trakt.core.reactions.media.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.local.UserMediaReactionsLocalSource
import tv.trakt.trakt.common.data.remote.MediaReactionsRemoteSource
import tv.trakt.trakt.common.model.reactions.MediaReactionsTarget
import tv.trakt.trakt.common.model.reactions.UserMediaReaction

internal class LoadUserMediaReactionsUseCase(
    private val remoteSource: MediaReactionsRemoteSource,
    private val localSource: UserMediaReactionsLocalSource,
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
