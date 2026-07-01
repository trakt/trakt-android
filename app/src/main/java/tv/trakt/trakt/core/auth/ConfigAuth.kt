package tv.trakt.trakt.core.auth

import androidx.appcompat.app.AppCompatDelegate
import tv.trakt.trakt.BuildConfig
import tv.trakt.trakt.common.Config
import java.util.Locale

internal object ConfigAuth {
    const val OAUTH_REDIRECT_URI = "trakt://auth"

    val authCodeUrl: String
        get() {
            val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
            return "${Config.WEB_AUTH_URL}oauth/authorize" +
                "?response_type=code" +
                "&client_id=${BuildConfig.TRAKT_API_KEY}" +
                "&redirect_uri=$OAUTH_REDIRECT_URI" +
                "&lang=${locale.language}-${locale.country}"
        }
}
