package tv.trakt.trakt.widgets

import android.content.Context
import android.content.Intent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import tv.trakt.trakt.MainActivity

internal const val INTENT_WIDGET_TARGET_EXTRA = "widgetTargetExtra"
private const val ACTION_WIDGET_OPEN = "tv.trakt.trakt.widgets.action.OPEN"

@Serializable
internal sealed interface WidgetIntentTarget {
    @Serializable
    data class Show(
        val showId: Int,
    ) : WidgetIntentTarget

    @Serializable
    data class Episode(
        val showId: Int,
        val episodeId: Int,
        val season: Int,
        val number: Int,
    ) : WidgetIntentTarget

    @Serializable
    data class Movie(
        val movieId: Int,
    ) : WidgetIntentTarget

    @Serializable
    data object Calendar : WidgetIntentTarget

    @Serializable
    data object UpNext : WidgetIntentTarget
}

internal fun Context.widgetTargetIntent(target: WidgetIntentTarget): Intent {
    return widgetIntent(key = target.key)
        .putExtra(INTENT_WIDGET_TARGET_EXTRA, Json.encodeToString(target))
}

private fun Context.widgetIntent(key: String): Intent {
    return Intent(this, MainActivity::class.java)
        .setAction("$ACTION_WIDGET_OPEN.$key")
        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
}

private val WidgetIntentTarget.key: String
    get() = when (this) {
        is WidgetIntentTarget.Show -> "show.$showId"
        is WidgetIntentTarget.Episode -> "episode.$showId.$episodeId"
        is WidgetIntentTarget.Movie -> "movie.$movieId"
        is WidgetIntentTarget.Calendar -> "calendar"
        is WidgetIntentTarget.UpNext -> "upnext"
    }
