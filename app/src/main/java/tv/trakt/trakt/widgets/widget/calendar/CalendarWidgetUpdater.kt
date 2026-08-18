package tv.trakt.trakt.widgets.widget.calendar

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.widgets.widget.calendar.data.CalendarWidgetDataSource

/**
 * Pushes app-side changes into the home-screen widget. Callers own the "did anything change"
 * question: every refresh costs requests plus a bitmap decode per card.
 */
internal class CalendarWidgetUpdater(
    private val context: Context,
    private val dataSource: CalendarWidgetDataSource,
    private val scope: CoroutineScope,
) {
    /** No-ops for ids owned by other widgets, so config can render every updater blindly. */
    suspend fun render(appWidgetId: Int) {
        try {
            val manager = GlanceAppWidgetManager(context)
            val glanceId = manager.getGlanceIdBy(appWidgetId)
            if (glanceId !in manager.getGlanceIds(CalendarWidget::class.java)) return

            CalendarWidget(dataSource).update(context, glanceId)
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.w(error, "Failed to render the Calendar widget %d", appWidgetId)
            }
        }
    }

    fun refreshInBackground() {
        scope.launch { refresh() }
    }

    suspend fun refresh() {
        try {
            GlanceAppWidgetManager(context)
                .getGlanceIds(CalendarWidget::class.java)
                .also { if (it.isEmpty()) return }

            dataSource.refresh(context = context, limit = MAX_ITEM_COUNT)
            CalendarWidget(dataSource).updateAll(context)
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.w(error, "Failed to refresh the Calendar widget")
            }
        }
    }
}
