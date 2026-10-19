package tv.trakt.trakt.core.home.sections.activity.data.local.social

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem

internal class HomeSocialLocalSource {
    private val mutex = Mutex()
    private val storage = mutableMapOf<Long, HomeActivityItem>()

    suspend fun addItems(items: List<HomeActivityItem>) {
        mutex.withLock {
            with(storage) {
                putAll(items.associateBy { it.id })
            }
        }
    }

    suspend fun setItems(items: List<HomeActivityItem>) {
        mutex.withLock {
            with(storage) {
                clear()
                putAll(items.associateBy { it.id })
            }
        }
    }

    suspend fun getItems(): List<HomeActivityItem> {
        return mutex.withLock {
            storage.values.toList()
        }
    }

    fun clear() {
        storage.clear()
    }
}
