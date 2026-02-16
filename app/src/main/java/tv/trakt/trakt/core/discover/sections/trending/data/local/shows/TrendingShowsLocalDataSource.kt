package tv.trakt.trakt.core.discover.sections.trending.data.local.shows

import tv.trakt.trakt.core.discover.model.DiscoverItem

internal interface TrendingShowsLocalDataSource {
    suspend fun setShows(shows: List<DiscoverItem.ShowItem>)

    suspend fun getShows(): List<DiscoverItem.ShowItem>
}
