package tv.trakt.trakt.core.discover.sections.releases.data.local.shows

import tv.trakt.trakt.core.calendar.model.CalendarItem

internal interface ReleasesShowsLocalDataSource {
    suspend fun setItems(items: List<CalendarItem.EpisodeItem>)

    suspend fun getItems(): List<CalendarItem.EpisodeItem>
}
