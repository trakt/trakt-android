package tv.trakt.trakt.core.streamings.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.WatchnowApi
import tv.trakt.trakt.common.Config.API_BASE_URL
import tv.trakt.trakt.common.core.streamings.data.local.StreamingLocalDataSource
import tv.trakt.trakt.common.core.streamings.data.local.StreamingStorage
import tv.trakt.trakt.common.core.streamings.data.remote.StreamingApiClient
import tv.trakt.trakt.common.core.streamings.data.remote.StreamingRemoteDataSource
import tv.trakt.trakt.common.core.streamings.helpers.PriorityStreamingServiceProvider

internal val streamingsDataModule = module {
    single<StreamingRemoteDataSource> {
        StreamingApiClient(
            api = WatchnowApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = get(),
                httpClientConfig = get(named("clientConfig")),
            ),
        )
    }

    single<StreamingLocalDataSource> {
        StreamingStorage()
    }

    single<PriorityStreamingServiceProvider> {
        PriorityStreamingServiceProvider()
    }
}
