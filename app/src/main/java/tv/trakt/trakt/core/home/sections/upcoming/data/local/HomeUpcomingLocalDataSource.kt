package tv.trakt.trakt.core.home.sections.upcoming.data.local

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.calendar.model.CalendarItem
import java.time.Instant

internal interface HomeUpcomingLocalDataSource {
    suspend fun setItems(items: List<CalendarItem>)

    suspend fun getItems(): List<CalendarItem>

    suspend fun removeShowItems(
        showIds: List<TraktId>,
        notify: Boolean,
    )

    fun observeUpdates(): Flow<Instant?>

    fun clear()
}
