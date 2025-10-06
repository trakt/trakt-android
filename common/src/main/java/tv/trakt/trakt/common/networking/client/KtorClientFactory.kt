package tv.trakt.trakt.common.networking.client

import android.content.Context
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import tv.trakt.trakt.common.auth.TokenProvider
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider

internal class KtorClientFactory(
    private val cacheMarkerProvider: CacheMarkerProvider,
    private val tokenProvider: TokenProvider,
    private val sessionManager: SessionManager,
) {
    fun createClientConfig(context: Context): (HttpClientConfig<*>) -> Unit {
        return {
            it.applyConfig(
                context = context,
                cacheMarkerProvider = cacheMarkerProvider,
            )
        }
    }

    fun createAuthorizedClientConfig(context: Context): (HttpClientConfig<*>) -> Unit {
        return {
            it.applyConfig(
                context = context,
                cacheMarkerProvider = cacheMarkerProvider,
            )
            it.applyAuthorizationConfig(
                tokenProvider = tokenProvider,
                sessionManager = sessionManager,
            )
        }
    }

    fun createClientEngine(): HttpClientEngine {
        return OkHttp.create()
    }
}
