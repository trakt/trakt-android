package tv.trakt.trakt.widgets.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.widgets.model.WidgetAppearance
import tv.trakt.trakt.widgets.model.WidgetBackground

private val BACKGROUND_KEY = stringPreferencesKey("widgetBackground")
private val TITLE_VISIBLE_KEY = booleanPreferencesKey("widgetTitleVisible")

/**
 * Glance state rather than a DataStore of our own: it is scoped to a single widget and a write
 * followed by an update re-reads it, so the placed widget repaints without reloading its data.
 */
internal class WidgetAppearanceStore(
    private val context: Context,
) {
    suspend fun get(appWidgetId: Int): WidgetAppearance {
        val glanceId = glanceId(appWidgetId) ?: return WidgetAppearance()

        return getAppWidgetState(
            context = context,
            definition = PreferencesGlanceStateDefinition,
            glanceId = glanceId,
        ).widgetAppearance()
    }

    suspend fun setBackground(
        appWidgetId: Int,
        background: WidgetBackground,
    ) {
        val glanceId = glanceId(appWidgetId) ?: return

        updateAppWidgetState(context = context, glanceId = glanceId) { preferences ->
            preferences[BACKGROUND_KEY] = background.name
        }
    }

    suspend fun setTitleVisible(
        appWidgetId: Int,
        visible: Boolean,
    ) {
        val glanceId = glanceId(appWidgetId) ?: return

        updateAppWidgetState(context = context, glanceId = glanceId) { preferences ->
            preferences[TITLE_VISIBLE_KEY] = visible
        }
    }

    private suspend fun glanceId(appWidgetId: Int): GlanceId? {
        return try {
            GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.w(error, "No Glance id bound to widget %d yet", appWidgetId)
            }
            null
        }
    }
}

/** Read from the Glance composition so saving a choice recomposes the widget in place. */
internal fun Preferences.widgetAppearance(): WidgetAppearance {
    val default = WidgetAppearance()

    return WidgetAppearance(
        background = this[BACKGROUND_KEY]
            ?.let { name -> WidgetBackground.entries.firstOrNull { it.name == name } }
            ?: default.background,
        titleVisible = this[TITLE_VISIBLE_KEY] ?: default.titleVisible,
    )
}
