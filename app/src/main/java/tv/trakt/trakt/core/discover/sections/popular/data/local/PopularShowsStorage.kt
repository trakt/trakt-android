package tv.trakt.trakt.core.discover.sections.popular.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant

internal class PopularShowsStorage : PopularShowsLocalDataSource {
    private val mutex = Mutex()
    private val showsCache = mutableMapOf<TraktId, Show>()

    override suspend fun addShows(
        shows: List<Show>,
        addedAt: Instant,
    ) {
        mutex.withLock {
            with(showsCache) {
                clear()
                putAll(shows.associateBy { it.ids.trakt })
            }
        }
    }

    override suspend fun getShows(): List<Show> {
        return mutex.withLock {
            showsCache.values.toList()
        }
    }
}
