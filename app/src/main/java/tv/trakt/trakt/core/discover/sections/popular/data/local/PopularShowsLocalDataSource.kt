package tv.trakt.trakt.core.discover.sections.popular.data.local

import tv.trakt.trakt.common.model.Show
import java.time.Instant

internal interface PopularShowsLocalDataSource {
    suspend fun addShows(
        shows: List<Show>,
        addedAt: Instant = Instant.now(),
    )

    suspend fun getShows(): List<Show>
}
