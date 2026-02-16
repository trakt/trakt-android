package tv.trakt.trakt.core.discover.sections.anticipated.data.local.shows

import tv.trakt.trakt.core.discover.model.DiscoverItem

internal interface AnticipatedShowsLocalDataSource {
    suspend fun setShows(shows: List<DiscoverItem.ShowItem>)

    suspend fun getShows(): List<DiscoverItem.ShowItem>
}
