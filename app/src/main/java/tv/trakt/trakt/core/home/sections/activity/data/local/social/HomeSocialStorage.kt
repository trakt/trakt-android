package tv.trakt.trakt.core.home.sections.activity.data.local.social

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem

internal class HomeSocialStorage : HomeSocialLocalDataSource {
    private val mutex = Mutex()
    private val storage = mutableMapOf<Long, HomeActivityItem>()

    override suspend fun addItems(items: List<HomeActivityItem>) {
        mutex.withLock {
            with(storage) {
                putAll(items.associateBy { it.id })
            }
        }
    }

    override suspend fun setItems(items: List<HomeActivityItem>) {
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

    override fun clear() {
        storage.clear()
    }
}
