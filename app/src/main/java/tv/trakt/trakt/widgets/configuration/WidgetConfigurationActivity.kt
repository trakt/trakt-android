package tv.trakt.trakt.widgets.configuration

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import tv.trakt.trakt.ui.theme.TraktTheme

/**
 * The launcher, not the app, starts this to configure one placed widget, so it sits outside the
 * single-activity NavHost by platform requirement.
 */
internal class WidgetConfigurationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // Leaving without saving cancels the placement on the platforms that gate adding on this
        // screen; on API 31+ the widget is already placed and the result is ignored.
        setResult(RESULT_CANCELED, widgetResult(appWidgetId))
        enableEdgeToEdge()

        setContent {
            TraktTheme {
                WidgetConfigurationScreen(
                    appWidgetId = appWidgetId,
                    onDone = {
                        setResult(RESULT_OK, widgetResult(appWidgetId))
                        finish()
                    },
                )
            }
        }
    }

    private fun widgetResult(appWidgetId: Int): Intent {
        return Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    }
}
