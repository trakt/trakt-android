package tv.trakt.app.tv.core.shows.data.local

import kotlinx.collections.immutable.persistentMapOf
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.shows.model.Show
import java.util.concurrent.ConcurrentHashMap

// TODO Temporary cache implementation, replace with a proper database solution later.
internal class ShowStorage : ShowLocalDataSource {
    private val cache = ConcurrentHashMap<TraktId, Show>(persistentMapOf())

    override suspend fun getShow(showId: TraktId): Show? {
        return cache[showId]
    }

    override suspend fun upsertShows(shows: List<Show>) {
        val map = shows.associateBy { it.ids.trakt }
        cache.putAll(map)
    }
}
