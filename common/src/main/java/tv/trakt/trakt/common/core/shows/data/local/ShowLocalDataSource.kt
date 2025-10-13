package tv.trakt.trakt.common.core.shows.data.local

import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId

interface ShowLocalDataSource {
    suspend fun getShow(showId: TraktId): Show?

    suspend fun upsertShows(shows: List<Show>)
}
