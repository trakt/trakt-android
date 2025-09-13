package tv.trakt.trakt.core.sync.di

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.SyncApi
import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.common.Config.API_HD_BASE_URL
import tv.trakt.trakt.core.sync.data.remote.movies.MoviesSyncApiClient
import tv.trakt.trakt.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.trakt.core.sync.data.remote.shows.ShowsSyncApiClient
import tv.trakt.trakt.core.sync.data.remote.shows.ShowsSyncRemoteDataSource
import tv.trakt.trakt.core.sync.usecases.ChangeMovieHistoryUseCase

internal val syncModule = module {
    single<ShowsSyncRemoteDataSource> {
        val httpClientEngine = get<HttpClientEngine>()
        val httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("authorizedClientConfig"))

        ShowsSyncApiClient(
            syncApi = SyncApi(
                baseUrl = API_HD_BASE_URL,
                httpClientEngine = httpClientEngine,
                httpClientConfig = httpClientConfig,
            ),
            usersApi = UsersApi(
                baseUrl = API_HD_BASE_URL,
                httpClientEngine = httpClientEngine,
                httpClientConfig = httpClientConfig,
            ),
        )
    }

    single<MoviesSyncRemoteDataSource> {
        val httpClientEngine = get<HttpClientEngine>()
        val httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("authorizedClientConfig"))

        MoviesSyncApiClient(
            usersApi = UsersApi(
                baseUrl = API_HD_BASE_URL,
                httpClientEngine = httpClientEngine,
                httpClientConfig = httpClientConfig,
            ),
            syncApi = SyncApi(
                baseUrl = API_HD_BASE_URL,
                httpClientEngine = httpClientEngine,
                httpClientConfig = httpClientConfig,
            ),
        )
    }

    factory {
        ChangeMovieHistoryUseCase(
            remoteSource = get(),
        )
    }
}
