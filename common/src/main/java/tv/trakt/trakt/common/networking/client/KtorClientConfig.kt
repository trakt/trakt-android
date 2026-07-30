package tv.trakt.trakt.common.networking.client

import com.google.firebase.crashlytics.CustomKeysAndValues
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.CacheStorage
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType.Application
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import timber.log.Timber
import tv.trakt.trakt.common.BuildConfig
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.auth.TokenProvider
import tv.trakt.trakt.common.auth.model.TraktAccessToken
import tv.trakt.trakt.common.auth.model.TraktRefreshToken
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private const val HEADER_TRAKT_API_KEY = "trakt-api-key"
private const val HEADER_TRAKT_API_VERSION = "trakt-api-version"
private const val TRAKT_API_VERSION_VALUE = 2

private val TIMEOUT_DURATION = 15.seconds

private val jsonNegotiation = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

private val mutex = Mutex()

internal fun HttpClientConfig<*>.applyConfig(
    fileStorage: CacheStorage,
    cacheMarkerProvider: CacheMarkerProvider,
) {
    expectSuccess = true

    install(UserAgent) {
        agent = Config.apiUserAgent()
    }

    install(HttpCache) {
        publicStorage(fileStorage)
    }

    install(HttpTimeout) {
        val timeoutMillis = TIMEOUT_DURATION.inWholeMilliseconds
        requestTimeoutMillis = timeoutMillis
        socketTimeoutMillis = timeoutMillis
        connectTimeoutMillis = timeoutMillis
    }

    install(HttpRequestRetry) {
        retryOnServerErrors(3)
        retryOnExceptionIf(3) { _, cause ->
            cause !is CancellationException
        }
        exponentialDelay()
    }

    install(ContentNegotiation) {
        json(jsonNegotiation)
    }

    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                Timber.i(message)
            }
        }
        level = when {
            BuildConfig.DEBUG -> LogLevel.ALL
            else -> LogLevel.NONE
        }
        sanitizeHeader { !BuildConfig.DEBUG && it == HttpHeaders.Authorization }
    }

    install(CacheBusterPlugin) {
        this.cacheMarkerProvider = cacheMarkerProvider
    }

    if (BuildConfig.DEBUG && BuildConfig.DEBUG_DELAY_ENABLED) {
        install(NetworkDelayPlugin)
    }

    defaultRequest {
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
                Timber.d("Loading auth tokens")
                tokenProvider.getToken()?.let { token ->
                    BearerTokens(
                        accessToken = token.accessToken,
                        refreshToken = token.refreshToken,
                    )
                }
            }

            refreshTokens {
                Timber.d("Refresh tokens requested")
                mutex.withLock {
                    val oldTokens = this.oldTokens
                    val currentTokens = tokenProvider.getToken()
                    if (currentTokens == null) {
                        Timber.d("No auth token available, skipping refresh")
                        return@withLock null
                    }

                    if (oldTokens?.accessToken != currentTokens.accessToken) {
                        Timber.d("Tokens already refreshed by another request")
                        return@withLock BearerTokens(
                            accessToken = currentTokens.accessToken,
                            refreshToken = currentTokens.refreshToken,
                        )
                    }

                    if (currentTokens.refreshToken.isBlank()) {
                        sessionManager.clear()
                        tokenProvider.clear()
                        Timber.e("Blank refresh token, clearing session")
                        return@withLock null
                    }

                    try {
                        Timber.d("Refreshing auth tokens")
                        val newTokens = client.post("${Config.WEB_AUTH_URL}oauth/token") {
                            setBody(
                                TraktRefreshToken(
                                    refreshToken = currentTokens.refreshToken,
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

                        return@withLock BearerTokens(
                            accessToken = newTokens.accessToken,
                            refreshToken = newTokens.refreshToken,
                        ).also {
                            Timber.d("Auth tokens refreshed successfully")
                        }
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: ClientRequestException) {
                        // 4xx = refresh token rejected by server, session is dead.
                        sessionManager.clear()
                        tokenProvider.clear()

                        Timber.e(error, "Refresh token rejected, clearing session")
                        Timber.recordError(
                            error = error,
                            keysValues = CustomKeysAndValues.Builder()
                                .putString("message", "Refresh token rejected.")
                                .build(),
                        )

                        return@withLock null
                    } catch (error: Exception) {
                        // Transient failure (offline, timeout, 5xx) — keep session,
                        // fail only this request; refresh retries on next 401.
                        Timber.w(error, "Failed to refresh auth tokens, keeping session")
                        return@withLock null
                    }
                }
            }
        }
    }
}

private class CacheBusterPluginConfig {
    var cacheMarkerProvider: CacheMarkerProvider? = null
}

/**
 * Plugin that adds a cache buster parameter to GET requests to avoid caching issues.
 */
private val CacheBusterPlugin = createClientPlugin("CacheBusterPlugin", ::CacheBusterPluginConfig) {
    val cacheMarkerProvider = pluginConfig.cacheMarkerProvider

    onRequest { request, _ ->
        if (request.method == HttpMethod.Get) {
            request.parameter(
                "cache_buster",
                cacheMarkerProvider?.getMarker(),
            )
        }
    }
}

/**
 * Plugin that adds a fixed delay to each request to simulate network delay for debugging purposes.
 */
private val NetworkDelayPlugin = createClientPlugin("DebugDelayPlugin") {
    onRequest { _, _ ->
        delay(3000.milliseconds)
    }
}
