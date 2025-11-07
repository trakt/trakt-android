package tv.trakt.trakt.core.discover.sections.anticipated.data.local.shows

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.discover.model.DiscoverItem
import java.time.Instant

internal class AnticipatedShowsStorage : AnticipatedShowsLocalDataSource {
    private val mutex = Mutex()
    private val showsCache = mutableMapOf<TraktId, DiscoverItem.ShowItem>()

    override suspend fun addShows(
        shows: List<DiscoverItem.ShowItem>,
        addedAt: Instant,
    ) {
        mutex.withLock {
            with(showsCache) {
                clear()
                putAll(shows.associateBy { it.show.ids.trakt })
            }
        }
    }

    override suspend fun getShows(): List<DiscoverItem.ShowItem> {
        return mutex.withLock {
            showsCache.values.toList()
        }
    }
}
