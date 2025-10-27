package tv.trakt.trakt.common.helpers.streamingservices

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.net.toUri

object StreamingServiceLink {
    fun openApp(
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

    fun PackageManager.getPackageInfoCompat(
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
}
