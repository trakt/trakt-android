package tv.trakt.trakt.core.home.sections.upnext.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.upnext.model.ProgressShow

internal class HomeUpNextStorage : HomeUpNextLocalDataSource {
    private val mutex = Mutex()
    private val storage = mutableMapOf<TraktId, ProgressShow>()

    override suspend fun addItems(items: List<ProgressShow>) {
        mutex.withLock {
            with(storage) {
                clear()
                putAll(items.associateBy { it.show.ids.trakt })
            }
        }
    }

    override suspend fun getItems(): List<ProgressShow> {
        return mutex.withLock {
            storage.values.toList()
        }
    }

    override fun clear() {
        storage.clear()
    }
}
