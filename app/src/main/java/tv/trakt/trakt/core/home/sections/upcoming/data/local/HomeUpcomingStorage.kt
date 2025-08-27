package tv.trakt.trakt.core.home.sections.upcoming.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.core.home.sections.upcoming.model.CalendarShow

internal class HomeUpcomingStorage : HomeUpcomingLocalDataSource {
    private val mutex = Mutex()
    private val storage = mutableMapOf<Long, CalendarShow>()

    override suspend fun addItems(items: List<CalendarShow>) {
        mutex.withLock {
            with(storage) {
                clear()
                putAll(items.associateBy { it.episode.ids.trakt.value })
            }
        }
    }

    override suspend fun getItems(): List<CalendarShow> {
        return mutex.withLock {
            storage.values.sortedBy { it.releaseAt }
        }
    }
}
