package tv.trakt.trakt.widgets.continuewatching.actions

import android.content.Context
import android.widget.Toast
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.widgets.continuewatching.ContinueWatchingWidget
import tv.trakt.trakt.widgets.continuewatching.data.ContinueWatchingWidgetDataSource
import tv.trakt.trakt.widgets.continuewatching.usecases.WidgetAddToHistoryUseCase

internal val ITEM_KEY_PARAM = ActionParameters.Key<String>("itemKey")
internal val EPISODE_ID_PARAM = ActionParameters.Key<Int>("episodeId")

/** Marks the tapped episode watched, mirroring the check button on the Up Next home section. */
internal class MarkWatchedAction :
    ActionCallback,
    KoinComponent {
    private val dataSource: ContinueWatchingWidgetDataSource by inject()
    private val addToHistoryUseCase: WidgetAddToHistoryUseCase by inject()

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val itemKey = parameters[ITEM_KEY_PARAM] ?: return
        val episodeId = parameters[EPISODE_ID_PARAM] ?: return

        val widget = ContinueWatchingWidget(dataSource)

        dataSource.setPendingItem(itemKey)
        // A live session recomposes off the data source; this covers an idle widget without one.
        widget.update(context, glanceId)

        try {
            addToHistoryUseCase.addToHistory(episodeId = episodeId.toTraktId())
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Timber.w(error, "Failed to mark episode %d as watched from the widget", episodeId)
            showError(context)
        } finally {
            dataSource.setPendingItem(null)
            widget.update(context, glanceId)
        }
    }

    /** The widget has no snackbar host, so a failure surfaces the same way a launcher error would. */
    private suspend fun showError(context: Context) {
        withContext(Dispatchers.Main) {
            Toast
                .makeText(
                    context,
                    R.string.error_text_unexpected_error_short,
                    Toast.LENGTH_SHORT,
                )
                .show()
        }
    }
}
