package tv.trakt.trakt.core.lists.sections.collaborations.data.local.items

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.CustomListItem

internal class ListsCollaborationsItemsStorage : ListsCollaborationsItemsLocalDataSource {
    private val mutex = Mutex()

    private val storage = mutableMapOf<TraktId, List<CustomListItem>>()

    override suspend fun setItems(
        listId: TraktId,
        items: List<CustomListItem>,
    ) {
        mutex.withLock {
            storage[listId] = items
        }
    }

    override suspend fun getItems(listId: TraktId): List<CustomListItem> {
        return mutex.withLock {
            storage[listId] ?: emptyList()
        }
    }

    override fun clear() {
        storage.clear()
    }
}
