package tv.trakt.trakt.widgets.continuewatching

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import tv.trakt.trakt.widgets.continuewatching.data.ContinueWatchingWidgetDataSource

internal class ContinueWatchingWidgetReceiver :
    GlanceAppWidgetReceiver(),
    KoinComponent {
    private val dataSource: ContinueWatchingWidgetDataSource by inject()

    override val glanceAppWidget: GlanceAppWidget
        get() = ContinueWatchingWidget(dataSource)
}
