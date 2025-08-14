package tv.trakt.trakt.core.shows.sections.hot.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.shows.model.WatchersShow
import java.time.Instant

internal class HotShowsStorage : HotShowsLocalDataSource {
    private val mutex = Mutex()
    private val showsCache = mutableMapOf<TraktId, WatchersShow>()

    override suspend fun addShows(
        shows: List<WatchersShow>,
        addedAt: Instant,
    ) {
        mutex.withLock {
            with(showsCache) {
                clear()
                putAll(shows.associateBy { it.show.ids.trakt })
            }
        }
    }

    override suspend fun getShows(): List<WatchersShow> {
        return mutex.withLock {
            showsCache.values.toList()
        }
    }
}
