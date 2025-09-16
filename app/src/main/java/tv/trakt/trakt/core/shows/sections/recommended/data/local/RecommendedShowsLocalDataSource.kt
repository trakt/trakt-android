package tv.trakt.trakt.core.shows.sections.recommended.data.local

import tv.trakt.trakt.common.model.Show
import java.time.Instant

internal interface RecommendedShowsLocalDataSource {
    suspend fun addShows(
        shows: List<Show>,
        addedAt: Instant = Instant.now(),
    )

    suspend fun getShows(): List<Show>

    suspend fun clear()
}
