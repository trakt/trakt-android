package tv.trakt.trakt.widgets.continuewatching

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.widgets.continuewatching.data.ContinueWatchingWidgetDataSource

/**
 * Pushes app-side changes into the home-screen widget. Callers own the "did anything change"
 * question: every refresh costs a request plus a bitmap decode per card.
 */
internal class ContinueWatchingWidgetUpdater(
    private val context: Context,
    private val dataSource: ContinueWatchingWidgetDataSource,
    private val scope: CoroutineScope,
) {
    /** No-ops for ids owned by other widgets, so config can render every updater blindly. */
    suspend fun render(appWidgetId: Int) {
        try {
            val manager = GlanceAppWidgetManager(context)
            val glanceId = manager.getGlanceIdBy(appWidgetId)
            if (glanceId !in manager.getGlanceIds(ContinueWatchingWidget::class.java)) return

            ContinueWatchingWidget(dataSource).update(context, glanceId)
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.w(error, "Failed to render the Continue Watching widget %d", appWidgetId)
            }
        }
    }

    fun refreshInBackground() {
        scope.launch { refresh() }
    }

    suspend fun refresh() {
        try {
            GlanceAppWidgetManager(context)
                .getGlanceIds(ContinueWatchingWidget::class.java)
                .also { if (it.isEmpty()) return }

            dataSource.refresh(context = context, limit = MAX_ITEM_COUNT)
            ContinueWatchingWidget(dataSource).updateAll(context)
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.w(error, "Failed to refresh the Continue Watching widget")
            }
        }
    }
}
