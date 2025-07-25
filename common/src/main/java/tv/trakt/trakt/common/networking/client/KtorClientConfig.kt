package tv.trakt.trakt.common.networking.client

import android.util.Log
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType.Application
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import tv.trakt.trakt.common.BuildConfig
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.auth.TokenProvider
import tv.trakt.trakt.common.auth.model.TraktAccessToken
import tv.trakt.trakt.common.auth.model.TraktRefreshToken
import tv.trakt.trakt.common.auth.session.SessionManager
import kotlin.time.Duration.Companion.seconds

private const val HEADER_TRAKT_API_KEY = "trakt-api-key"
private const val HEADER_TRAKT_API_VERSION = "trakt-api-version"
private const val TRAKT_API_VERSION_VALUE = 2

private val TIMEOUT_DURATION = 15.seconds

private val jsonNegotiation = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

internal fun HttpClientConfig<*>.applyConfig(baseUrl: String) {
    expectSuccess = true

    install(HttpCache)

    install(HttpTimeout) {
        val timeoutMillis = TIMEOUT_DURATION.inWholeMilliseconds
        requestTimeoutMillis = timeoutMillis
        socketTimeoutMillis = timeoutMillis
        connectTimeoutMillis = timeoutMillis
    }

    install(ContentNegotiation) {
        json(jsonNegotiation)
    }

    install(Logging) {
        logger = Logger.ANDROID
        level = if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.NONE
    }

    defaultRequest {
        url(baseUrl)
        header(HttpHeaders.ContentType, Application.Json)
        header(HEADER_TRAKT_API_KEY, BuildConfig.TRAKT_API_KEY)
        header(HEADER_TRAKT_API_VERSION, TRAKT_API_VERSION_VALUE)
    }
}

internal fun HttpClientConfig<*>.applyAuthorizationConfig(
    tokenProvider: TokenProvider,
    sessionManager: SessionManager,
) {
    install(Auth) {
        bearer {
            loadTokens {
                val token = tokenProvider.getToken()!!
                BearerTokens(
                    accessToken = token.accessToken,
                    refreshToken = token.refreshToken,
                )
            }
            refreshTokens {
                try {
                    Log.d("HttpClient", "Refreshing auth tokens")
                    val newTokens = client.post("${Config.API_BASE_URL}oauth/token") {
                        setBody(
                            TraktRefreshToken(
                                refreshToken = oldTokens?.refreshToken!!,
                                clientId = BuildConfig.TRAKT_API_KEY,
                                clientSecret = BuildConfig.TRAKT_API_SECRET,
                                type = "refresh_token",
                            ),
                        )
                    }.body<TraktAccessToken>()

                    tokenProvider.saveToken(
                        TraktAccessToken(
                            accessToken = newTokens.accessToken,
                            refreshToken = newTokens.refreshToken,
                            expiresIn = newTokens.expiresIn,
                            createdAt = newTokens.createdAt,
                        ),
                    )

                    return@refreshTokens BearerTokens(
                        accessToken = newTokens.accessToken,
                        refreshToken = newTokens.refreshToken,
                    ).also {
                        Log.d("HttpClient", "Auth tokens refreshed successfully")
                    }
                } catch (error: Exception) {
                    sessionManager.clear()
                    tokenProvider.clear()
                    return@refreshTokens null.also {
                        Log.e("HttpClient", "Failed to refresh auth tokens", error)
                    }
                }
            }
        }
    }
}
