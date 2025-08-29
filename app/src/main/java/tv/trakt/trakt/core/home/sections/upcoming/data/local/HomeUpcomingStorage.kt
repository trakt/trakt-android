package tv.trakt.trakt.core.home.sections.upcoming.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.upcoming.model.HomeUpcomingItem

internal class HomeUpcomingStorage : HomeUpcomingLocalDataSource {
    private val mutex = Mutex()
    private val storage = mutableMapOf<TraktId, HomeUpcomingItem>()

    override suspend fun addItems(items: List<HomeUpcomingItem>) {
        mutex.withLock {
            with(storage) {
                clear()
                putAll(items.associateBy { it.id })
            }
        }
    }

    override suspend fun getItems(): List<HomeUpcomingItem> {
        return mutex.withLock {
            storage.values.toList()
        }
    }

    override fun clear() {
        storage.clear()
    }
}
