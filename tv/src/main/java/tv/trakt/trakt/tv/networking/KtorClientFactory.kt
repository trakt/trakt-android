package tv.trakt.trakt.tv.networking

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import tv.trakt.trakt.common.auth.TokenProvider
import tv.trakt.trakt.tv.auth.session.SessionManager

internal class KtorClientFactory(
    private val baseUrl: String,
    private val tokenProvider: TokenProvider,
    private val sessionManager: SessionManager,
) {
    fun createClientConfig(): (HttpClientConfig<*>) -> Unit {
        return {
            it.applyConfig(baseUrl)
        }
    }

    fun createAuthorizedClientConfig(): (HttpClientConfig<*>) -> Unit {
        return {
            it.applyConfig(baseUrl)
            it.applyAuthorizationConfig(
                tokenProvider = tokenProvider,
                sessionManager = sessionManager,
            )
        }
    }

    fun createClientEngine(): HttpClientEngine {
        return OkHttp.create()
    }

    fun createClient(): HttpClient {
        return HttpClient(engine = createClientEngine()) {
            applyConfig(baseUrl)
        }
    }
}
