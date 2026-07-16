package tv.trakt.trakt.core.auth

import androidx.appcompat.app.AppCompatDelegate
import tv.trakt.trakt.BuildConfig
import tv.trakt.trakt.common.Config
import java.util.Locale

internal object ConfigAuth {
    const val OAUTH_REDIRECT_URI = "trakt://auth"

    /**
     * Builds the OAuth authorization URL for the given PKCE [codeVerifier] (RFC 7636). The
     * verifier is generated and persisted by the caller so it survives process death while the
     * user is away in the external browser; this function stays pure.
     */
    fun authCodeUrl(codeVerifier: String): String {
        val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
        return "${Config.WEB_AUTH_URL}oauth/authorize" +
            "?response_type=code" +
            "&client_id=${BuildConfig.TRAKT_API_KEY}" +
            "&redirect_uri=$OAUTH_REDIRECT_URI" +
            "&code_challenge=${Pkce.codeChallenge(codeVerifier)}" +
            "&code_challenge_method=S256" +
            "&lang=${locale.language}-${locale.country}"
    }
}
