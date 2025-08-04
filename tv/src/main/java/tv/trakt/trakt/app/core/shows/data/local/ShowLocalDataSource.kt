package tv.trakt.trakt.app.core.shows.data.local

import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId

internal interface ShowLocalDataSource {
    suspend fun getShow(showId: TraktId): Show?

    suspend fun upsertShows(shows: List<Show>)
}
