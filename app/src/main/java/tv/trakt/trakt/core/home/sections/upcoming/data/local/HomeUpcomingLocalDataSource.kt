package tv.trakt.trakt.core.home.sections.upcoming.data.local

import tv.trakt.trakt.core.home.sections.upcoming.model.CalendarShow

internal interface HomeUpcomingLocalDataSource {
    suspend fun addItems(items: List<CalendarShow>)

    suspend fun getItems(): List<CalendarShow>
}
