package tv.trakt.trakt.core.discover.sections.recommended.data.local.shows

import tv.trakt.trakt.core.discover.model.DiscoverItem
import java.time.Instant

internal interface RecommendedShowsLocalDataSource {
    suspend fun addShows(
        shows: List<DiscoverItem.ShowItem>,
        addedAt: Instant = Instant.now(),
    )

    suspend fun getShows(): List<DiscoverItem.ShowItem>

    suspend fun clear()
}
