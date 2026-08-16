package tv.trakt.trakt.common.networking.client

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.cache.storage.CacheStorage
import tv.trakt.trakt.common.auth.TokenProvider
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider

internal class KtorClientFactory(
    private val cacheMarkerProvider: CacheMarkerProvider,
    private val tokenProvider: TokenProvider,
    private val sessionManager: SessionManager,
    private val fileStorage: CacheStorage,
) {
    fun createClientConfig(): (HttpClientConfig<*>) -> Unit {
        return {
            it.applyConfig(
                fileStorage = fileStorage,
                cacheMarkerProvider = cacheMarkerProvider,
            )
        }
    }

    fun createAuthorizedClientConfig(): (HttpClientConfig<*>) -> Unit {
        return {
            it.applyConfig(
                fileStorage = fileStorage,
                cacheMarkerProvider = cacheMarkerProvider,
            )
            it.applyAuthorizationConfig(
                tokenProvider = tokenProvider,
                sessionManager = sessionManager,
            )
        }
    }

    fun createKlipyClientConfig(): (HttpClientConfig<*>) -> Unit {
        return {
            it.applyKlipyConfig(fileStorage = fileStorage)
        }
    }

    fun createClientEngine(): HttpClientEngine {
        return OkHttp.create()
    }
}
