package tv.trakt.app.tv.core.sync.di

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.SyncApi
import org.openapitools.client.apis.UsersApi
import org.openapitools.client.apis.WatchedApi
import tv.trakt.app.tv.Config.API_BASE_URL
import tv.trakt.app.tv.core.sync.data.local.episodes.EpisodesSyncLocalDataSource
import tv.trakt.app.tv.core.sync.data.local.episodes.EpisodesSyncStorage
import tv.trakt.app.tv.core.sync.data.local.movies.MoviesSyncLocalDataSource
import tv.trakt.app.tv.core.sync.data.local.movies.MoviesSyncStorage
import tv.trakt.app.tv.core.sync.data.local.shows.ShowsSyncLocalDataSource
import tv.trakt.app.tv.core.sync.data.local.shows.ShowsSyncStorage
import tv.trakt.app.tv.core.sync.data.remote.episodes.EpisodesSyncApiClient
import tv.trakt.app.tv.core.sync.data.remote.episodes.EpisodesSyncRemoteDataSource
import tv.trakt.app.tv.core.sync.data.remote.movies.MoviesSyncApiClient
import tv.trakt.app.tv.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.app.tv.core.sync.data.remote.shows.ShowsSyncApiClient
import tv.trakt.app.tv.core.sync.data.remote.shows.ShowsSyncRemoteDataSource

internal val syncModule = module {
    single<ShowsSyncRemoteDataSource> {
        val httpClientEngine = get<HttpClientEngine>()
        val httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("authorizedClientConfig"))

        ShowsSyncApiClient(
            usersApi = UsersApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = httpClientEngine,
                httpClientConfig = httpClientConfig,
            ),
            syncApi = SyncApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = httpClientEngine,
                httpClientConfig = httpClientConfig,
            ),
            watchedApi = WatchedApi(
                baseUrl = API_BASE_URL,
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
                baseUrl = API_BASE_URL,
                httpClientEngine = httpClientEngine,
                httpClientConfig = httpClientConfig,
            ),
            syncApi = SyncApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = httpClientEngine,
                httpClientConfig = httpClientConfig,
            ),
            watchedApi = WatchedApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = httpClientEngine,
                httpClientConfig = httpClientConfig,
            ),
        )
    }

    single<EpisodesSyncRemoteDataSource> {
        val httpClientEngine = get<HttpClientEngine>()
        val httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("authorizedClientConfig"))

        EpisodesSyncApiClient(
            syncApi = SyncApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = httpClientEngine,
                httpClientConfig = httpClientConfig,
            ),
        )
    }

    single<ShowsSyncLocalDataSource> {
        ShowsSyncStorage()
    }

    single<MoviesSyncLocalDataSource> {
        MoviesSyncStorage()
    }

    single<EpisodesSyncLocalDataSource> {
        EpisodesSyncStorage()
    }
}
