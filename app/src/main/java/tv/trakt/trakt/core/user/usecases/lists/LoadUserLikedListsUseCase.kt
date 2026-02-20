package tv.trakt.trakt.core.user.usecases.lists

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.user.data.local.liked.UserLikedListsLocalDataSource
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource
import java.time.Instant

/**
 * Use case for loading user's liked lists.
 */
internal class LoadUserLikedListsUseCase(
    private val remoteSource: UserRemoteDataSource,
    private val localSource: UserLikedListsLocalDataSource,
) {
    suspend fun isLoaded(): Boolean {
        return localSource.isLoaded()
    }

    suspend fun loadLocalLists(): ImmutableMap<TraktId, Instant> {
        return localSource.getLists()
            .toImmutableMap()
    }

    suspend fun loadLists(): ImmutableMap<TraktId, Instant> {
        val listsResponse = remoteSource
            .getLikedLists(minimal = true)
            .associateBy(
                keySelector = { it.list.id?.toTraktId()!! },
                valueTransform = { it.likedAt.toInstant() },
            )

        return listsResponse
            .toImmutableMap()
            .also {
                localSource.setLists(lists = listsResponse)
            }
    }
}
