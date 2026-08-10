package tv.trakt.trakt.widgets.data

import tv.trakt.trakt.widgets.calendar.CalendarWidgetUpdater
import tv.trakt.trakt.widgets.continuewatching.ContinueWatchingWidgetUpdater
import tv.trakt.trakt.widgets.streaks.StreaksWidgetUpdater

internal class WidgetsUpdater(
    private val continueWatchingWidgetUpdater: ContinueWatchingWidgetUpdater,
    private val calendarWidgetUpdater: CalendarWidgetUpdater,
    private val streaksWidgetUpdater: StreaksWidgetUpdater,
) {
    fun refreshInBackground() {
        continueWatchingWidgetUpdater.refreshInBackground()
        calendarWidgetUpdater.refreshInBackground()
        streaksWidgetUpdater.refreshInBackground()
    }
}
