package tv.trakt.trakt.core.user.data.local

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.PersonalListItem
import tv.trakt.trakt.core.lists.model.PersonalListItem.MovieItem
import tv.trakt.trakt.core.lists.model.PersonalListItem.ShowItem
import java.time.Instant

internal class UserListsStorage : UserListsLocalDataSource {
    private val mutex = Mutex()

    private var storage: MutableMap<TraktId, Pair<CustomList, List<PersonalListItem>>>? = null
    private val updatedAt = MutableSharedFlow<Instant?>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private fun ensureInitialized() {
        if (storage == null) {
            storage = mutableMapOf()
        }
    }

    override suspend fun setLists(
        lists: Map<CustomList, List<PersonalListItem>>,
        notify: Boolean,
    ) {
        mutex.withLock {
            ensureInitialized()
            storage?.let { storage ->
                storage.clear()
                lists.forEach { (list, items) ->
                    storage[list.ids.trakt] = list to items
                }
            }

            if (notify) {
                updatedAt.tryEmit(nowUtcInstant())
            }
        }
    }

    override suspend fun addLists(
        lists: Map<CustomList, List<PersonalListItem>>,
        notify: Boolean,
    ) {
        mutex.withLock {
            ensureInitialized()
            storage?.let { storage ->
                lists.forEach { (list, items) ->
                    storage[list.ids.trakt] = list to items
                }
            }

            if (notify) {
                updatedAt.tryEmit(nowUtcInstant())
            }
        }
    }

    override suspend fun containsList(listId: TraktId): Boolean {
        return mutex.withLock {
            storage?.containsKey(listId) == true
        }
    }

    override suspend fun getListItems(listId: TraktId): List<PersonalListItem>? {
        return mutex.withLock {
            storage?.get(listId)?.second
        }
    }

    override suspend fun getLists(): Map<CustomList, List<PersonalListItem>> {
        return mutex.withLock {
            storage?.values?.associate { it.first to it.second } ?: emptyMap()
        }
    }

    override suspend fun isLoaded(): Boolean {
        return mutex.withLock {
            storage != null
        }
    }

    override suspend fun removeLists(
        listsIds: Set<TraktId>,
        notify: Boolean,
    ) {
        mutex.withLock {
            storage?.let { storage ->
                listsIds.forEach { id ->
                    storage.remove(id)
                }
            }

            if (notify) {
                updatedAt.tryEmit(nowUtcInstant())
            }
        }
    }

    override suspend fun addListItem(
        listId: TraktId,
        item: PersonalListItem,
        notify: Boolean,
    ) {
        mutex.withLock {
            storage?.let { storage ->
                val entry = storage[listId]
                if (entry != null) {
                    val (list, items) = entry
                    val updatedItems = items
                        .plus(item)
                        .distinctBy {
                            when (it) {
                                is MovieItem -> it.movie.ids.trakt
                                is ShowItem -> it.show.ids.trakt
                            }
                        }
                    storage[listId] = list to updatedItems
                }
            }

            if (notify) {
                updatedAt.tryEmit(nowUtcInstant())
            }
        }
    }

    override suspend fun removeListItem(
        listId: TraktId,
        itemId: TraktId,
        itemType: MediaType,
        notify: Boolean,
    ) {
        mutex.withLock {
            storage?.let { storage ->
                val entry = storage[listId]
                if (entry != null) {
                    val (list, items) = entry
                    val updatedItems = items.filterNot {
                        it.id == itemId &&
                            it.type == itemType
                    }
                    storage[listId] = list to updatedItems
                }
            }

            if (notify) {
                updatedAt.tryEmit(nowUtcInstant())
            }
        }
    }

    override fun observeUpdates(): Flow<Instant?> {
        return updatedAt
    }

    override fun clear() {
        storage?.clear()
        storage = null
        updatedAt.tryEmit(null)
    }
}
