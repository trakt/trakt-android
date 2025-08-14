package tv.trakt.trakt.core.shows.sections.anticipated.data.local

import tv.trakt.trakt.core.shows.model.WatchersShow
import java.time.Instant

internal interface AnticipatedShowsLocalDataSource {
    suspend fun addShows(
        shows: List<WatchersShow>,
        addedAt: Instant = Instant.now(),
    )

    suspend fun getShows(): List<WatchersShow>
}
