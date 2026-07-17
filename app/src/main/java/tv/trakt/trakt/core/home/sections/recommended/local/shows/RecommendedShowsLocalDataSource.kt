package tv.trakt.trakt.core.home.sections.recommended.local.shows

import tv.trakt.trakt.core.discover.model.DiscoverItem

internal interface RecommendedShowsLocalDataSource {
    suspend fun setShows(shows: List<DiscoverItem.ShowItem>)

    suspend fun getShows(): List<DiscoverItem.ShowItem>

    suspend fun clear()
}
