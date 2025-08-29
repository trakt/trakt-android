package tv.trakt.trakt.common.networking.di

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.Config.API_BASE_URL
import tv.trakt.trakt.common.auth.TokenProvider
import tv.trakt.trakt.common.networking.client.KtorClientFactory

val networkingModule = module {
    single<KtorClientFactory> {
        KtorClientFactory(
            baseUrl = API_BASE_URL,
            tokenProvider = get<TokenProvider>(),
            sessionManager = get(),
        )
    }

    single<HttpClientEngine> {
        val factory = get<KtorClientFactory>()
        factory.createClientEngine()
    }

    single<(HttpClientConfig<*>) -> Unit>(named("clientConfig")) {
        val factory = get<KtorClientFactory>()
        factory.createClientConfig(androidContext())
    }

    single<(HttpClientConfig<*>) -> Unit>(named("authorizedClientConfig")) {
        val factory = get<KtorClientFactory>()
        factory.createAuthorizedClientConfig(androidContext())
    }
}
