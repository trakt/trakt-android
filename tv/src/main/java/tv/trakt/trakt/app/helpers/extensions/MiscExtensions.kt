package tv.trakt.trakt.app.helpers.extensions

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.compose.ui.platform.UriHandler
import androidx.core.net.toUri

internal fun openWatchNowLink(
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
