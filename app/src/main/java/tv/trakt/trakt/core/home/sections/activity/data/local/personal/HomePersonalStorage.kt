package tv.trakt.trakt.core.home.sections.activity.data.local.personal

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem

internal class HomePersonalStorage : HomePersonalLocalDataSource {
    private val mutex = Mutex()
    private val storage = mutableMapOf<Long, HomeActivityItem>()

    override suspend fun addItems(items: List<HomeActivityItem>) {
        mutex.withLock {
            with(storage) {
                clear()
                putAll(items.associateBy { it.id })
            }
        }
    }

    override suspend fun getItems(): List<HomeActivityItem> {
        return mutex.withLock {
            storage.values.toList()
        }
    }

    override suspend fun removeItems(ids: Set<Long>) {
        mutex.withLock {
            ids.forEach {
                storage.remove(it)
            }
        }
    }

    override fun clear() {
        storage.clear()
    }
}
