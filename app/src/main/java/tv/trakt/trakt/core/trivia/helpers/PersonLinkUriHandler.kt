package tv.trakt.trakt.core.trivia.helpers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.core.net.toUri
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.core.applinks.model.AppLink
import tv.trakt.trakt.core.applinks.model.parseAppLink

@Composable
internal fun rememberPersonLinkUriHandler(onPersonClick: (SlugId) -> Unit): UriHandler {
    val fallback = LocalUriHandler.current
    val currentOnPersonClick by rememberUpdatedState(onPersonClick)

    return remember(fallback) {
        object : UriHandler {
            override fun openUri(uri: String) {
                val appLink = parseAppLink(uri.toUri())
                if (appLink !is AppLink.Person) {
                    fallback.openUri(uri)
                    return
                }

                currentOnPersonClick(appLink.slug)
            }
        }
    }
}
