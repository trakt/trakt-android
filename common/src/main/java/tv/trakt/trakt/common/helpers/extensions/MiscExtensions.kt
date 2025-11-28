package tv.trakt.trakt.common.helpers.extensions

import android.app.UiModeManager
import android.content.Context
import android.content.Context.UI_MODE_SERVICE
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration.UI_MODE_TYPE_TELEVISION
import android.net.Uri
import android.os.Build
import androidx.compose.ui.platform.UriHandler
import androidx.core.net.toUri
import tv.trakt.trakt.common.Config.PLEX_BASE_URL
import tv.trakt.trakt.common.model.SeasonEpisode

fun openWatchNowLink(
    context: Context,
    uriHandler: UriHandler,
    link: String?,
) {
    if (link.isNullOrBlank()) {
        return
    }

    if (link.contains("netflix", ignoreCase = true)) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = link.toUri()
        intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("source", "30")
        context.startActivity(intent)
        return
    }

    uriHandler.openUri(link)
}

fun openPlexLink(
    uriHandler: UriHandler,
    type: String,
    slug: String?,
    episode: SeasonEpisode? = null,
) {
    if (type !in setOf("movie", "show", "episode")) {
        return
    }

    if (slug.isNullOrBlank()) {
        return
    }

    val linkType = when (type) {
        "episode" -> "show"
        else -> type
    }
    val plexUrl = "$PLEX_BASE_URL$linkType/$slug"

    uriHandler.openUri(
        when (type) {
            "episode" -> "$plexUrl/season/${episode?.season}/episode/${episode?.episode}"
            else -> plexUrl
        },
    )
}

fun openExternalAppLink(
    packageId: String?,
    packageName: String?,
    uri: Uri?,
    context: Context,
) {
    if (uri == null) {
        return
    }

    val packageManager = context.packageManager
    val intent = Intent(Intent.ACTION_VIEW, uri)

    if (packageId.isNullOrEmpty()) {
        try {
            val storeLink = "https://play.google.com/store/search?q=$packageName&c=apps"
            val storeIntent =
                Intent(Intent.ACTION_VIEW, storeLink.toUri())
            context.startActivity(storeIntent)
            return
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
    }

    // Try to open deep link in the app
    if (isAppInstalled(packageManager, packageId)) {
        intent.setPackage(packageId)
        try {
            context.startActivity(intent)
            return
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Fallback: open in Play Store if not installed
    try {
        val storeIntent = Intent(Intent.ACTION_VIEW, "market://details?id=$packageId".toUri())
        context.startActivity(storeIntent)
        return
    } catch (_: Exception) {
        // Fallback in case Play Store not available
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=$packageId".toUri(),
            ),
        )
    }

    // Fallback: open in Play Store search
    try {
        val storeLink = "https://play.google.com/store/search?q=$packageName&c=apps"
        val storeIntent =
            Intent(Intent.ACTION_VIEW, storeLink.toUri())
        context.startActivity(storeIntent)
    } catch (_: Exception) {
        // NOOP
    }
}

private fun isAppInstalled(
    packageManager: PackageManager,
    packageName: String,
): Boolean {
    return try {
        packageManager.getPackageInfoCompat(packageName)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }
}

private fun PackageManager.getPackageInfoCompat(
    packageName: String,
    flags: Int = 0,
): PackageInfo {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
    } else {
        @Suppress("DEPRECATION")
        getPackageInfo(packageName, flags)
    }
}

/**
 * Checks if the device is a TV device.
 */
fun Context.isTelevision(): Boolean {
    val uiModeManager = getSystemService(UI_MODE_SERVICE) as UiModeManager
    val packageManager = applicationContext.packageManager

    return uiModeManager.currentModeType == UI_MODE_TYPE_TELEVISION &&
        packageManager.hasSystemFeature("android.software.leanback")
}
