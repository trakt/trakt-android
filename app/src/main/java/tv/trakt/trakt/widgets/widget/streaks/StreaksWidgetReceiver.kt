package tv.trakt.trakt.widgets.widget.streaks

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import tv.trakt.trakt.widgets.widget.streaks.data.StreaksWidgetDataSource

internal class StreaksWidgetReceiver :
    GlanceAppWidgetReceiver(),
    KoinComponent {
    private val dataSource: StreaksWidgetDataSource by inject()

    override val glanceAppWidget: GlanceAppWidget
        get() = StreaksWidget(dataSource)
}
