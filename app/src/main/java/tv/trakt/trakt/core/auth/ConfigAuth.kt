package tv.trakt.trakt.core.auth

import tv.trakt.trakt.BuildConfig
import tv.trakt.trakt.common.Config

internal object ConfigAuth {
    const val OAUTH_REDIRECT_URI = "trakt://auth"

    val authCodeUrl: String
        get() = "${Config.WEB_BASE_URL}oauth/authorize" +
            "?response_type=code" +
            "&client_id=${BuildConfig.TRAKT_API_KEY}" +
            "&redirect_uri=$OAUTH_REDIRECT_URI"
}
