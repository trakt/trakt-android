package tv.trakt.trakt.core.discover.sections.anticipated.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.discover.model.WatchersShow
import java.time.Instant

internal class AnticipatedShowsStorage : AnticipatedShowsLocalDataSource {
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
