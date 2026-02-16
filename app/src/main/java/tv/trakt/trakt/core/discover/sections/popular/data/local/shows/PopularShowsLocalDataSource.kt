package tv.trakt.trakt.core.discover.sections.popular.data.local.shows

import tv.trakt.trakt.core.discover.model.DiscoverItem

internal interface PopularShowsLocalDataSource {
    suspend fun setShows(shows: List<DiscoverItem.ShowItem>)

    suspend fun getShows(): List<DiscoverItem.ShowItem>
}
