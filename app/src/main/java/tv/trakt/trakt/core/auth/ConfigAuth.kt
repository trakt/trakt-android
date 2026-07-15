package tv.trakt.trakt.core.auth

import androidx.appcompat.app.AppCompatDelegate
import tv.trakt.trakt.BuildConfig
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.core.auth.ConfigAuth.authCodeUrl
import java.util.Locale

internal object ConfigAuth {
    const val OAUTH_REDIRECT_URI = "trakt://auth"

    /**
     * PKCE verifier for the in-flight sign-in attempt (RFC 7636). Generated alongside
     * [authCodeUrl], consumed once the redirect delivers the authorization code back.
     */
    private var codeVerifier: String? = null

    /**
     * Returns the verifier generated with the most recent [authCodeUrl] and clears it,
     * so a verifier is never reused across sign-in attempts.
     */
    fun consumeCodeVerifier(): String? {
        val verifier = codeVerifier
        codeVerifier = null
        return verifier
    }

    val authCodeUrl: String
        get() {
            val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
            val verifier = Pkce.generateCodeVerifier()
            codeVerifier = verifier
            return "${Config.WEB_AUTH_URL}oauth/authorize" +
                "?response_type=code" +
                "&client_id=${BuildConfig.TRAKT_API_KEY}" +
                "&redirect_uri=$OAUTH_REDIRECT_URI" +
                "&code_challenge=${Pkce.codeChallenge(verifier)}" +
                "&code_challenge_method=S256" +
                "&lang=${locale.language}-${locale.country}"
        }
}
