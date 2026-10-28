package tv.trakt.trakt.core.inreview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler

/**
 * [UriHandler] that opens Year and Month in Review links in the in-app web view.
 * Other links open with the current handler.
 */
@Composable
internal fun rememberInReviewUriHandler(onInReviewClick: (url: String) -> Unit): UriHandler {
    val fallback = LocalUriHandler.current
    val currentOnInReviewClick by rememberUpdatedState(onInReviewClick)

    return remember(fallback) {
        object : UriHandler {
            override fun openUri(uri: String) {
                if (isInReviewUrl(uri)) {
                    currentOnInReviewClick(uri)
                    return
                }
                fallback.openUri(uri)
            }
        }
    }
}
