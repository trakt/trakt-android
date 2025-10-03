package tv.trakt.trakt.common.helpers.extensions

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
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
