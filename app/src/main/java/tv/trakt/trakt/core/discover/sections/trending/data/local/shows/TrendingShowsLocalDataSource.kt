package tv.trakt.trakt.core.discover.sections.trending.data.local.shows

import tv.trakt.trakt.core.discover.model.DiscoverItem
import java.time.Instant

internal interface TrendingShowsLocalDataSource {
    suspend fun addShows(
        shows: List<DiscoverItem.ShowItem>,
        addedAt: Instant = Instant.now(),
    )

    suspend fun getShows(): List<DiscoverItem.ShowItem>
}
