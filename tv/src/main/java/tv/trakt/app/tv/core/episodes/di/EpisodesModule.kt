package tv.trakt.app.tv.core.episodes.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.ShowsApi
import org.openapitools.client.apis.UsersApi
import tv.trakt.app.tv.Config.API_BASE_URL
import tv.trakt.app.tv.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.app.tv.core.episodes.data.local.EpisodeStorage
import tv.trakt.app.tv.core.episodes.data.remote.EpisodesApiClient
import tv.trakt.app.tv.core.episodes.data.remote.EpisodesRemoteDataSource

internal val episodesDataModule = module {
    single<EpisodesRemoteDataSource> {
        EpisodesApiClient(
            showsApi = ShowsApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = get(),
                httpClientConfig = get(named("clientConfig")),
            ),
            usersApi = UsersApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = get(),
                httpClientConfig = get(named("authorizedClientConfig")),
            ),
        )
    }

    single<EpisodeLocalDataSource> {
        EpisodeStorage()
    }
}
