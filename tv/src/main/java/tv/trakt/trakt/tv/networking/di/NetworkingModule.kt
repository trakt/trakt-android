package tv.trakt.trakt.tv.networking.di

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.tv.Config.API_BASE_URL
import tv.trakt.trakt.tv.networking.KtorClientFactory

internal val networkingModule = module {
    single<KtorClientFactory> {
        KtorClientFactory(
            baseUrl = API_BASE_URL,
            tokenProvider = get(),
            sessionManager = get(),
        )
    }

    single<HttpClientEngine> {
        val factory = get<KtorClientFactory>()
        factory.createClientEngine()
    }

    single<(HttpClientConfig<*>) -> Unit>(named("clientConfig")) {
        val factory = get<KtorClientFactory>()
        factory.createClientConfig()
    }

    single<(HttpClientConfig<*>) -> Unit>(named("authorizedClientConfig")) {
        val factory = get<KtorClientFactory>()
        factory.createAuthorizedClientConfig()
    }

    single<HttpClient> {
        val ktorClientFactory = get<KtorClientFactory>()
        ktorClientFactory.createClient()
    }
}
