package tv.trakt.trakt.core.lists.sections.smart.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.user.data.remote.smartlists.UserSmartListsRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.lists.sections.smart.data.local.ListsSmartLocalDataSource

internal class GetSmartListsUseCase(
    private val remoteSource: UserSmartListsRemoteDataSource,
    private val localSource: ListsSmartLocalDataSource,
) {
    suspend fun getLocalSmartLists(pagination: Pagination? = null): ImmutableList<SmartList> {
        return localSource.getLists()
            .take(pagination?.limit ?: Int.MAX_VALUE)
            .toImmutableList()
    }

    suspend fun getSmartLists(pagination: Pagination? = null): ImmutableList<SmartList> {
        return remoteSource.getSmartLists()
            .take(pagination?.limit ?: Int.MAX_VALUE)
            .asyncMap {
                SmartList.fromDto(it)
            }
            .asyncMap {
                // Fetch the first 8 items of the smart list to get their images and fill the list.
                it.copy(
                    images = Images(
                        posters = remoteSource.getSmartListItems(
                            listId = it.ids.trakt,
                            type = "all",
                            pagination = Pagination(
                                page = 1,
                                limit = 8,
                            ),
                            extended = "images",
                        ).mapNotNull { item ->
                            when {
                                item.show != null -> item.show?.images?.poster?.firstOrNull()
                                item.movie != null -> item.movie?.images?.poster?.firstOrNull()
                                else -> null
                            }
                        }.toImmutableList(),
                    ),
                )
            }
            .toImmutableList()
            .also {
                localSource.setLists(it)
            }
    }
}
