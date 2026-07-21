package tv.trakt.trakt.common.core.user.usecases.lists

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.core.user.data.local.UserListsLocalDataSource
import tv.trakt.trakt.common.core.user.data.remote.personallists.UserPersonalListsRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.CustomListMinimal
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.toTraktId

/**
 * Loads the user's lists from the remote source and updates the local cache.
 */
class LoadUserListsUseCase(
    private val remoteSource: UserPersonalListsRemoteDataSource,
    private val localSource: UserListsLocalDataSource,
) {
    suspend fun isLoaded(): Boolean {
        return localSource.isLoaded()
    }

    suspend fun loadLocalLists(): ImmutableMap<TraktId, CustomListMinimal> {
        return localSource.getLists()
            .toImmutableMap()
    }

    suspend fun loadMovieLists(movieId: TraktId): ImmutableSet<TraktId> {
        return remoteSource.getMovieLists(movieId).toImmutableSet()
    }

    suspend fun loadShowLists(showId: TraktId): ImmutableSet<TraktId> {
        return remoteSource.getShowLists(showId).toImmutableSet()
    }

    suspend fun loadLists(): ImmutableMap<TraktId, CustomListMinimal> =
        coroutineScope {
            val lists = remoteSource.getPersonalListsMinimal()
                .asyncMap {
                    CustomListMinimal(
                        id = it.id.toTraktId(),
                        ownerId = it.ownerId.toTraktId(),
                        name = it.name,
                        type = it.type,
                        itemsCount = it.count,
                        displayOrder = it.displayOrder,
                    )
                }

            return@coroutineScope lists
                .sortedWith(
                    compareBy<CustomListMinimal> { it.displayOrder }
                        .thenByDescending { it.itemsCount },
                )
                .associateBy { it.id }
                .toImmutableMap()
                .also {
                    localSource.setLists(lists = lists)
                }
        }
}
