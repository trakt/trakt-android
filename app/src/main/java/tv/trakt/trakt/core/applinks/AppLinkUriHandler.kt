package tv.trakt.trakt.core.applinks

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

/**
 * [UriHandler] for markdown text that routes app.trakt.tv links to the matching callback.
 * Links without a callback open with the current handler.
 */
@Composable
internal fun rememberAppLinkUriHandler(
    onShowClick: ((SlugId) -> Unit)? = null,
    onMovieClick: ((SlugId) -> Unit)? = null,
    onPersonClick: ((SlugId) -> Unit)? = null,
): UriHandler {
    val fallback = LocalUriHandler.current
    val currentOnShowClick by rememberUpdatedState(onShowClick)
    val currentOnMovieClick by rememberUpdatedState(onMovieClick)
    val currentOnPersonClick by rememberUpdatedState(onPersonClick)

    return remember(fallback) {
        object : UriHandler {
            override fun openUri(uri: String) {
                val handled = when (val appLink = parseAppLink(uri.toUri())) {
                    is AppLink.Show -> currentOnShowClick?.invoke(appLink.slug)
                    is AppLink.Movie -> currentOnMovieClick?.invoke(appLink.slug)
                    is AppLink.Person -> currentOnPersonClick?.invoke(appLink.slug)
                    is AppLink.Imdb, null -> null
                }

                if (handled == null) {
                    fallback.openUri(uri)
                }
            }
        }
    }
}
