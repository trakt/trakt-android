package tv.trakt.trakt.core.auth.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import tv.trakt.trakt.common.BuildConfig
import tv.trakt.trakt.common.auth.model.TraktAccessToken
import tv.trakt.trakt.core.auth.ConfigAuth
import tv.trakt.trakt.core.auth.data.remote.model.TokenExchangeRequest

internal class AuthApiClient(
    private val baseUrl: String,
    httpClientEngine: HttpClientEngine,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit),
) : AuthRemoteDataSource {
    private val httpClient = HttpClient(httpClientEngine) {
        httpClientConfig.invoke(this)
    }

    override suspend fun getAccessToken(
        code: String,
        codeVerifier: String?,
    ): TraktAccessToken {
        val request = TokenExchangeRequest(
            code = code,
            clientId = BuildConfig.TRAKT_API_KEY,
            clientSecret = BuildConfig.TRAKT_API_SECRET,
            redirectUri = ConfigAuth.OAUTH_REDIRECT_URI,
            codeVerifier = codeVerifier,
            grantType = "authorization_code",
        )

        return httpClient.post {
            url("${baseUrl}oauth/token")
            setBody(request)
        }.body<TraktAccessToken>()
    }
}
