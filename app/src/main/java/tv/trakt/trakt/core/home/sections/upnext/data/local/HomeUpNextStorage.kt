package tv.trakt.trakt.core.home.sections.upnext.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.upnext.model.ProgressShow
import java.time.Instant

internal class HomeUpNextStorage : HomeUpNextLocalDataSource {
    private val mutex = Mutex()
    private val storage = mutableMapOf<TraktId, ProgressShow>()

    override suspend fun addItems(
        shows: List<ProgressShow>,
        addedAt: Instant,
    ) {
        mutex.withLock {
            with(storage) {
                clear()
                putAll(shows.associateBy { it.show.ids.trakt })
            }
        }
    }

    override suspend fun getItems(): List<ProgressShow> {
        return mutex.withLock {
            storage.values.toList()
        }
    }
}
