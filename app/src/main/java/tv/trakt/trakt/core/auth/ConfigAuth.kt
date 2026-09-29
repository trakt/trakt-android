package tv.trakt.trakt.core.auth

import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import tv.trakt.trakt.BuildConfig
import tv.trakt.trakt.common.Config
import java.util.Locale

internal object ConfigAuth {
    const val OAUTH_REDIRECT_SCHEME = "trakt"
    const val OAUTH_REDIRECT_URI = "$OAUTH_REDIRECT_SCHEME://auth"

    private const val OAUTH_HTTPS_REDIRECT_HOST = "app.trakt.tv"
    private const val OAUTH_HTTPS_REDIRECT_PATH = "/callback/app/trakt"
    const val OAUTH_HTTPS_REDIRECT_URI = "https://$OAUTH_HTTPS_REDIRECT_HOST$OAUTH_HTTPS_REDIRECT_PATH"

    fun isHttpsRedirect(uri: Uri): Boolean =
        uri.scheme == "https" &&
            uri.host == OAUTH_HTTPS_REDIRECT_HOST &&
            uri.path == OAUTH_HTTPS_REDIRECT_PATH

    /**
     * Builds the OAuth authorization URL for the given PKCE [codeVerifier] (RFC 7636). The
     * verifier is generated and persisted by the caller so it survives process death while the
     * user is away in the external browser; this function stays pure.
     */
    fun authCodeUrl(
        codeVerifier: String,
        redirectUri: String,
    ): String {
        val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
        return "${Config.WEB_AUTH_URL}oauth/authorize" +
            "?response_type=code" +
            "&client_id=${BuildConfig.TRAKT_API_KEY}" +
            "&redirect_uri=${Uri.encode(redirectUri)}" +
            "&code_challenge=${Pkce.codeChallenge(codeVerifier)}" +
            "&code_challenge_method=S256" +
            "&lang=${locale.language}-${locale.country}"
    }
}
