package tv.trakt.trakt.common.auth.di

import io.ktor.client.HttpClientConfig
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.Config.WEB_AUTH_URL
import tv.trakt.trakt.common.auth.data.remote.AuthApiClient
import tv.trakt.trakt.common.auth.data.remote.AuthRemoteDataSource

val authRemoteModule = module {
    single<AuthRemoteDataSource> {
        AuthApiClient(
            baseUrl = WEB_AUTH_URL,
            oauthApi = get(),
            httpClientEngine = get(),
            httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("clientConfig")),
        )
    }
}
