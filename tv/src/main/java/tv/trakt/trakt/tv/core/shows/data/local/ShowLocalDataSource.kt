package tv.trakt.trakt.tv.core.shows.data.local

import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.tv.core.shows.model.Show

internal interface ShowLocalDataSource {
    suspend fun getShow(showId: TraktId): Show?

    suspend fun upsertShows(shows: List<Show>)
}
