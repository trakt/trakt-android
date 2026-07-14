import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo

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
    } catch (e: ActivityNotFoundException) {
        // TODO: Show error
    }
}

fun Context.googleTranslateActivityInfo() =
    queryProcessTextActivities()
        .firstOrNull { it.activityInfo.packageName == "com.google.android.apps.translate" }
        ?.activityInfo

fun Context.queryProcessTextActivities(): List<ResolveInfo> {
    val intent = Intent()
        .setAction(Intent.ACTION_PROCESS_TEXT)
        .setType("text/plain")
    return packageManager.queryIntentActivities(intent, 0)
}

fun Context.isGoogleTranslateInstalled() =
    queryProcessTextActivities()
        .any { it.activityInfo.packageName == "com.google.android.apps.translate" }
