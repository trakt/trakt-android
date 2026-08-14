package tv.trakt.trakt.widgets.streaks

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.widgets.streaks.data.StreaksWidgetDataSource
import kotlin.time.Duration.Companion.seconds

internal class StreaksWidgetUpdater(
    private val context: Context,
    private val dataSource: StreaksWidgetDataSource,
    private val scope: CoroutineScope,
) {
    suspend fun render(appWidgetId: Int) {
        try {
            val manager = GlanceAppWidgetManager(context)
            val glanceId = manager.getGlanceIdBy(appWidgetId)
            if (glanceId !in manager.getGlanceIds(StreaksWidget::class.java)) return

            StreaksWidget(dataSource).update(context, glanceId)
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.w(error, "Failed to render the Streaks widget %d", appWidgetId)
            }
        }
    }

    fun refreshInBackground() {
        scope.launch {
            // Give a moment to register a just-posted event before the streak is recomputed.
            delay(3.seconds)
            refresh()
        }
    }

    suspend fun refresh() {
        try {
            GlanceAppWidgetManager(context)
                .getGlanceIds(StreaksWidget::class.java)
                .also { if (it.isEmpty()) return }

            dataSource.refresh()
            StreaksWidget(dataSource).updateAll(context)
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.w(error, "Failed to refresh the Streaks widget")
            }
        }
    }
}
