package tv.trakt.app.tv.core.shows.data.local

import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.shows.model.Show

internal interface ShowLocalDataSource {
    suspend fun getShow(showId: TraktId): Show?

    suspend fun upsertShows(shows: List<Show>)
}
