package tv.trakt.trakt.widgets.calendar

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import tv.trakt.trakt.widgets.calendar.data.CalendarWidgetDataSource

internal class CalendarWidgetReceiver :
    GlanceAppWidgetReceiver(),
    KoinComponent {
    private val dataSource: CalendarWidgetDataSource by inject()

    override val glanceAppWidget: GlanceAppWidget
        get() = CalendarWidget(dataSource)
}
