package tv.trakt.trakt.core.shows.sections.trending.data.local

import tv.trakt.trakt.core.shows.model.WatchersShow
import java.time.Instant

internal interface TrendingShowsLocalDataSource {
    suspend fun addShows(
        shows: List<WatchersShow>,
        addedAt: Instant = Instant.now(),
    )

    suspend fun getShows(): List<WatchersShow>
}
