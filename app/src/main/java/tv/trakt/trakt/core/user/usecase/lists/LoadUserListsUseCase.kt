package tv.trakt.trakt.core.user.usecase.lists

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.ListItemDto
import tv.trakt.trakt.core.lists.model.PersonalListItem
import tv.trakt.trakt.core.user.data.local.UserListsLocalDataSource
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource

/**
 * Loads the user's lists from the remote source and updates the local cache.
 */
internal class LoadUserListsUseCase(
    private val remoteSource: UserRemoteDataSource,
    private val localSource: UserListsLocalDataSource,
) {
    suspend fun isLoaded(): Boolean {
        return localSource.isLoaded()
    }

    suspend fun loadLocalLists(): ImmutableMap<CustomList, List<PersonalListItem>> {
        return localSource.getLists()
            .toImmutableMap()
    }

    suspend fun loadLists(): ImmutableMap<CustomList, List<PersonalListItem>> =
        coroutineScope {
            val listsResponse = remoteSource.getPersonalLists()
            val listsItemsResponse: List<Pair<ListDto, List<ListItemDto>>> = listsResponse
                .map { list ->
                    async {
                        val items = remoteSource.getPersonalListItems(
                            listId = list.ids.trakt.toTraktId(),
                            extended = "full,cloud9,colors",
                            limit = null, // -> all
                        )
                        list to items
                    }
                }.awaitAll()

            val itemsResponse = listsItemsResponse
                .asyncMap { (list, items) ->
                    val list = CustomList.fromDto(list)
                    val listItems = items.asyncMap {
                        val listedAt = it.listedAt.toInstant()
                        when {
                            it.movie != null -> {
                                PersonalListItem.MovieItem(
                                    movie = Movie.fromDto(it.movie!!),
                                    listedAt = listedAt,
                                )
                            }
                            it.show != null -> {
                                PersonalListItem.ShowItem(
                                    show = Show.fromDto(it.show!!),
                                    listedAt = listedAt,
                                )
                            }
                            else -> throw IllegalStateException("Personal list item unknown type!")
                        }
                    }
                    list to listItems
                }.toMap()

            localSource.setLists(lists = itemsResponse)
            itemsResponse.toImmutableMap()
        }
}
