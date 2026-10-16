package tv.trakt.trakt.common.auth.di

import io.ktor.client.HttpClientConfig
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.Config.WEB_AUTH_URL
import tv.trakt.trakt.common.auth.data.remote.AuthRemoteSource

val authRemoteModule = module {
    single {
        AuthRemoteSource(
            baseUrl = WEB_AUTH_URL,
            oauthApi = get(),
            httpClientEngine = get(),
            httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("clientConfig")),
        )
    }
}
