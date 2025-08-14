package tv.trakt.trakt.core.shows.sections.hot.data.local

import tv.trakt.trakt.core.shows.model.WatchersShow
import java.time.Instant

internal interface HotShowsLocalDataSource {
    suspend fun addShows(
        shows: List<WatchersShow>,
        addedAt: Instant = Instant.now(),
    )

    suspend fun getShows(): List<WatchersShow>
}
