package tv.trakt.trakt.common.helpers.extensions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo

private const val GOOGLE_TRANSLATE_PACKAGE = "com.google.android.apps.translate"
private var isGoogleTranslateInstalled: Boolean? = null

fun Context.openGoogleTranslate(
    activity: ActivityInfo,
    text: String,
) {
    val intent = Intent()
        .setAction(Intent.ACTION_PROCESS_TEXT)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_PROCESS_TEXT, text)
        .putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
        .putExtra("key_language_from", "auto")
        .setClassName(activity.packageName, activity.name)
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // TODO: Show error
    }
}

fun Context.googleTranslateActivityInfo(): ActivityInfo? {
    return queryProcessTextActivities()
        .firstOrNull { it.activityInfo.packageName == GOOGLE_TRANSLATE_PACKAGE }
        ?.activityInfo
}

fun Context.isGoogleTranslateInstalled(): Boolean {
    if (isGoogleTranslateInstalled != null) {
        return isGoogleTranslateInstalled ?: false
    }
    return queryProcessTextActivities()
        .any { it.activityInfo.packageName == GOOGLE_TRANSLATE_PACKAGE }
        .also { isGoogleTranslateInstalled = it }
}

private fun Context.queryProcessTextActivities(): List<ResolveInfo> {
    val intent = Intent()
        .setAction(Intent.ACTION_PROCESS_TEXT)
        .setType("text/plain")
    return packageManager.queryIntentActivities(intent, 0)
}
