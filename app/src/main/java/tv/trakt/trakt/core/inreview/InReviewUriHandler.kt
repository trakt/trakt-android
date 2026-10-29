package tv.trakt.trakt.core.inreview

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsIntent.COLOR_SCHEME_DARK
import androidx.browser.customtabs.CustomTabsIntent.COLOR_SCHEME_LIGHT
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.core.net.toUri
import timber.log.Timber
import tv.trakt.trakt.common.ui.theme.colors.TraktColors
import tv.trakt.trakt.ui.theme.TraktTheme

/**
 * [UriHandler] that opens Year and Month in Review links in a Custom Tab, or in the in-app
 * web view when no Custom Tab browser is available. Other links open with the current handler.
 */
@Composable
internal fun rememberInReviewUriHandler(
    slurm: String?,
    onInReviewClick: (url: String) -> Unit,
): UriHandler {
    val fallback = LocalUriHandler.current
    val context = LocalContext.current
    val currentSlurm by rememberUpdatedState(slurm)
    val currentColors by rememberUpdatedState(TraktTheme.colors)
    val currentOnInReviewClick by rememberUpdatedState(onInReviewClick)

    return remember(fallback, context) {
        object : UriHandler {
            override fun openUri(uri: String) {
                if (!isInReviewUrl(uri)) {
                    fallback.openUri(uri)
                    return
                }

                val webUri = toInReviewWebUrl(url = uri, slurm = currentSlurm).toUri()
                if (openCustomTab(context = context, uri = webUri, colors = currentColors)) {
                    return
                }
                currentOnInReviewClick(uri)
            }
        }
    }
}

private fun openCustomTab(
    context: Context,
    uri: Uri,
    colors: TraktColors,
): Boolean {
    val browser = CustomTabsClient.getPackageName(context, null) ?: return false

    val colorScheme = when {
        colors.isLight -> COLOR_SCHEME_LIGHT
        else -> COLOR_SCHEME_DARK
    }
    val colorSchemeParams = CustomTabColorSchemeParams.Builder()
        .setToolbarColor(colors.backgroundPrimary.toArgb())
        .setNavigationBarColor(colors.backgroundPrimary.toArgb())
        .build()

    return runCatching {
        CustomTabsIntent.Builder()
            .setColorScheme(colorScheme)
            .setDefaultColorSchemeParams(colorSchemeParams)
            .build()
            .apply { intent.setPackage(browser) }
            .launchUrl(context, uri)
    }.onFailure {
        Timber.w(it, "Failed to open Custom Tab")
    }.isSuccess
}
