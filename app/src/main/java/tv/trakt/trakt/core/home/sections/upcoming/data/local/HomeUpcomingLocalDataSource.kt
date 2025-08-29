package tv.trakt.trakt.core.home.sections.upcoming.data.local

import tv.trakt.trakt.core.home.sections.upcoming.model.HomeUpcomingItem

internal interface HomeUpcomingLocalDataSource {
    suspend fun addItems(items: List<HomeUpcomingItem>)

    suspend fun getItems(): List<HomeUpcomingItem>

    fun clear()
}
