package tv.trakt.app.tv.core.streamings.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.WatchnowApi
import tv.trakt.app.tv.Config.API_BASE_URL
import tv.trakt.app.tv.core.streamings.data.local.StreamingLocalDataSource
import tv.trakt.app.tv.core.streamings.data.local.StreamingStorage
import tv.trakt.app.tv.core.streamings.data.remote.StreamingApiClient
import tv.trakt.app.tv.core.streamings.data.remote.StreamingRemoteDataSource
import tv.trakt.app.tv.core.streamings.utilities.PriorityStreamingServiceProvider

internal val streamingsModule = module {
    single<StreamingRemoteDataSource> {
        StreamingApiClient(
            api = WatchnowApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = get(),
                httpClientConfig = get(named("clientConfig")),
            ),
        )
    }

    single<PriorityStreamingServiceProvider> {
        PriorityStreamingServiceProvider()
    }

    single<StreamingLocalDataSource> {
        StreamingStorage()
    }
}
