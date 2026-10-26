package tv.trakt.trakt.core.auth

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.browser.auth.AuthTabIntent
import androidx.browser.auth.AuthTabIntent.AuthResult
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.core.auth.ConfigAuth.OAUTH_HTTPS_REDIRECT_HOST
import tv.trakt.trakt.core.auth.ConfigAuth.OAUTH_HTTPS_REDIRECT_PATH
import tv.trakt.trakt.core.auth.ConfigAuth.OAUTH_HTTPS_REDIRECT_URI
import tv.trakt.trakt.core.auth.ConfigAuth.OAUTH_REDIRECT_SCHEME

/**
 * Opens the Trakt sign-in page, preferring an Auth Tab: it returns the redirect to the app as an
 * activity result, instead of relying on the browser to hand the redirect off as an App Link.
 * Falls back to a Custom Tab, then to the plain browser.
 */
internal class AuthBrowser(
    private val activity: ComponentActivity,
    onAuthTabResult: (AuthResult) -> Unit,
) {
    private val authTabLauncher = AuthTabIntent.registerActivityResultLauncher(
        activity,
        onAuthTabResult,
    )

    private val browserPackage: String?
        get() = CustomTabsClient.getPackageName(activity, null)

    fun supportsAuthTab(): Boolean {
        val browser = browserPackage ?: return false
        return CustomTabsClient.isAuthTabSupported(activity, browser)
    }

    fun open(
        url: Uri,
        redirectUri: String,
    ) {
        val browser = browserPackage
        when {
            browser == null -> openBrowser(url)
            CustomTabsClient.isAuthTabSupported(activity, browser) -> openAuthTab(browser, url, redirectUri)
            else -> openCustomTab(browser, url)
        }.also {
            val option = when {
                browser == null -> "Browser"
                CustomTabsClient.isAuthTabSupported(activity, browser) -> "Auth Tab"
                else -> "Custom Tab"
            }
            Timber.d("AuthBrowser: Opening $option for URL: $url")
        }
    }

    private fun openAuthTab(
        browser: String,
        url: Uri,
        redirectUri: String,
    ) {
        val authTabIntent = AuthTabIntent.Builder().build()
        authTabIntent.intent.setPackage(browser)

        when (redirectUri) {
            OAUTH_HTTPS_REDIRECT_URI -> authTabIntent.launch(
                authTabLauncher,
                url,
                OAUTH_HTTPS_REDIRECT_HOST,
                OAUTH_HTTPS_REDIRECT_PATH,
            )
            else -> authTabIntent.launch(authTabLauncher, url, OAUTH_REDIRECT_SCHEME)
        }
    }

    private fun openCustomTab(
        browser: String,
        url: Uri,
    ) {
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.intent.setPackage(browser)
        customTabsIntent.launchUrl(activity, url)
    }

    private fun openBrowser(url: Uri) {
        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW, url))
        } catch (error: ActivityNotFoundException) {
            Timber.recordError(error)
        }
    }
}
