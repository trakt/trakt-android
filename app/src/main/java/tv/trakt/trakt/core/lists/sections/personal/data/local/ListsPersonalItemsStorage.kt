package tv.trakt.trakt.core.lists.sections.personal.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.PersonalListItem

internal class ListsPersonalItemsStorage : ListsPersonalItemsLocalDataSource {
    private val mutex = Mutex()
    private val storage = mutableMapOf<TraktId, List<PersonalListItem>>()

    override suspend fun addItems(
        listId: TraktId,
        items: List<PersonalListItem>,
    ) {
        mutex.withLock {
            storage[listId] = items
        }
    }

    override suspend fun getItems(listId: TraktId): List<PersonalListItem> {
        return mutex.withLock {
            storage[listId] ?: emptyList()
        }
    }

//    override suspend fun addItems(items: List<CustomList>) {
//        mutex.withLock {
//            with(storage) {
//                clear()
//                putAll(items.associateBy { it.ids.trakt })
//            }
//        }
//    }
//
//    override suspend fun getItems(): List<CustomList> {
//        return mutex.withLock {
//            storage.values.toList()
//        }
//    }

    override fun clear() {
        storage.clear()
    }
}
