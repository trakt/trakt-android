package tv.trakt.trakt.widgets.data

internal class WidgetsUpdater(
    private val continueWatchingWidgetUpdater:
        tv.trakt.trakt.widgets.widget.continuewatching.ContinueWatchingWidgetUpdater,
    private val calendarWidgetUpdater: tv.trakt.trakt.widgets.widget.calendar.CalendarWidgetUpdater,
    private val streaksWidgetUpdater: tv.trakt.trakt.widgets.widget.streaks.StreaksWidgetUpdater,
) {
    fun refreshInBackground() {
        continueWatchingWidgetUpdater.refreshInBackground()
        calendarWidgetUpdater.refreshInBackground()
        streaksWidgetUpdater.refreshInBackground()
    }
}
