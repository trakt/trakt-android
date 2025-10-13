package tv.trakt.trakt.common.core.shows.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId

class ShowStorage : ShowLocalDataSource {
    private val mutex = Mutex()
    private val storage = mutableMapOf<TraktId, Show>()

    override suspend fun getShow(showId: TraktId): Show? {
        return mutex.withLock {
            storage[showId]
        }
    }

    override suspend fun upsertShows(shows: List<Show>) {
        mutex.withLock {
            with(storage) {
                putAll(shows.associateBy { it.ids.trakt })
            }
        }
    }
}
