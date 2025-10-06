package tv.trakt.trakt.common.networking.client

import android.content.Context
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.FileStorage
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.ANDROID
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
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider
import java.nio.file.Files
import kotlin.coroutines.cancellation.CancellationException
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
    context: Context,
    cacheMarkerProvider: CacheMarkerProvider,
) {
    expectSuccess = true

    install(HttpCache) {
        val cacheFile = Files.createDirectories(context.cacheDir.resolve("ktor").toPath()).toFile()
        publicStorage(FileStorage(cacheFile))
    }

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

    install(CacheBusterPlugin) {
        this.cacheMarkerProvider = cacheMarkerProvider
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
                val token = tokenProvider.getToken()
                    ?: throw IllegalStateException("No auth token available")
                BearerTokens(
                    accessToken = token.accessToken,
                    refreshToken = token.refreshToken,
                )
            }

            refreshTokens {
                Timber.d("Refresh tokens requested")
                mutex.withLock {
                    val oldTokens = this.oldTokens
                    val currentTokens = tokenProvider.getToken()!!

                    if (oldTokens?.accessToken != currentTokens.accessToken) {
                        Timber.d("Tokens already refreshed by another request")
                        return@withLock BearerTokens(
                            accessToken = currentTokens.accessToken,
                            refreshToken = currentTokens.refreshToken,
                        )
                    }

                    try {
                        Timber.d("Refreshing auth tokens")
                        val newTokens = client.post("${Config.API_BASE_URL}oauth/token") {
                            setBody(
                                TraktRefreshToken(
                                    refreshToken = oldTokens.refreshToken ?: "",
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
                    } catch (error: Exception) {
                        if (error !is CancellationException) {
                            sessionManager.clear()
                            tokenProvider.clear()
                        }
                        return@withLock null.also {
                            Timber.e(error, "Failed to refresh auth tokens")
                        }
                    }
                }
            }
        }
    }
}

private class CacheBusterPluginConfig {
    var cacheMarkerProvider: CacheMarkerProvider? = null
}

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
