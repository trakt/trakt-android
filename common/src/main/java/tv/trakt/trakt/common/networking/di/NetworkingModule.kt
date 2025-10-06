package tv.trakt.trakt.common.networking.di

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.CalendarsApi
import org.openapitools.client.apis.HistoryApi
import org.openapitools.client.apis.ListsApi
import org.openapitools.client.apis.MoviesApi
import org.openapitools.client.apis.OauthApi
import org.openapitools.client.apis.RecommendationsApi
import org.openapitools.client.apis.SearchApi
import org.openapitools.client.apis.ShowsApi
import org.openapitools.client.apis.SyncApi
import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.common.Config.API_BASE_URL
import tv.trakt.trakt.common.Config.API_HD_BASE_URL
import tv.trakt.trakt.common.networking.api.SyncExtrasApi
import tv.trakt.trakt.common.networking.client.KtorClientFactory
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider
import tv.trakt.trakt.common.networking.helpers.DefaultCacheMarkerProvider

val networkingModule = module {
    single<KtorClientFactory> {
        KtorClientFactory(
            cacheMarkerProvider = get(),
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
        factory.createClientConfig(androidContext())
    }

    single<(HttpClientConfig<*>) -> Unit>(named("authorizedClientConfig")) {
        val factory = get<KtorClientFactory>()
        factory.createAuthorizedClientConfig(androidContext())
    }

    single<CacheMarkerProvider> {
        DefaultCacheMarkerProvider()
    }
}

val networkingApiModule = module {
    single(named("apiClients")) {
        arrayOf(
            get<CalendarsApi>(),
            get<HistoryApi>(),
            get<ListsApi>(),
            get<MoviesApi>(),
            get<OauthApi>(),
            get<RecommendationsApi>(),
            get<SearchApi>(),
            get<ShowsApi>(),
            get<SyncApi>(),
            get<SyncExtrasApi>(),
            get<UsersApi>(),
        )
    }

    single<ShowsApi> {
        ShowsApi(
            baseUrl = API_HD_BASE_URL,
            httpClientEngine = get(),
            httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("clientConfig")),
        )
    }

    single<MoviesApi> {
        MoviesApi(
            baseUrl = API_HD_BASE_URL,
            httpClientEngine = get(),
            httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("clientConfig")),
        )
    }

    single<RecommendationsApi> {
        RecommendationsApi(
            baseUrl = API_HD_BASE_URL,
            httpClientEngine = get(),
            httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("authorizedClientConfig")),
        )
    }

    single<ListsApi> {
        ListsApi(
            baseUrl = API_HD_BASE_URL,
            httpClientEngine = get(),
            httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("authorizedClientConfig")),
        )
    }

    single<SearchApi> {
        SearchApi(
            baseUrl = API_HD_BASE_URL,
            httpClientEngine = get(),
            httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("clientConfig")),
        )
    }

    single<SearchApi>(named("authorizedSearchApi")) {
        SearchApi(
            baseUrl = API_HD_BASE_URL,
            httpClientEngine = get(),
            httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("authorizedClientConfig")),
        )
    }

    single<SyncApi> {
        SyncApi(
            baseUrl = API_HD_BASE_URL,
            httpClientEngine = get(),
            httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("authorizedClientConfig")),
        )
    }

    single<SyncExtrasApi> {
        SyncExtrasApi(
            baseUrl = API_HD_BASE_URL,
            httpClientEngine = get(),
            httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("authorizedClientConfig")),
        )
    }

    single<UsersApi> {
        UsersApi(
            baseUrl = API_HD_BASE_URL,
            httpClientEngine = get(),
            httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("authorizedClientConfig")),
        )
    }

    single<CalendarsApi> {
        CalendarsApi(
            baseUrl = API_HD_BASE_URL,
            httpClientEngine = get(),
            httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("authorizedClientConfig")),
        )
    }

    single<HistoryApi> {
        HistoryApi(
            baseUrl = API_HD_BASE_URL,
            httpClientEngine = get(),
            httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("authorizedClientConfig")),
        )
    }

    single<OauthApi> {
        OauthApi(
            baseUrl = API_BASE_URL,
            httpClientEngine = get(),
            httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("clientConfig")),
        )
    }
}
