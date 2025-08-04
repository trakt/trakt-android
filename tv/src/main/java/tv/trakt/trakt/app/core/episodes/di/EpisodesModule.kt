package tv.trakt.trakt.app.core.episodes.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.ShowsApi
import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.app.Config.API_BASE_URL
import tv.trakt.trakt.app.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.app.core.episodes.data.local.EpisodeStorage
import tv.trakt.trakt.app.core.episodes.data.remote.EpisodesApiClient
import tv.trakt.trakt.app.core.episodes.data.remote.EpisodesRemoteDataSource

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
