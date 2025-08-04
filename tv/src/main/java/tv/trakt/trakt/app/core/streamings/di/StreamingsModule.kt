package tv.trakt.trakt.app.core.streamings.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.WatchnowApi
import tv.trakt.trakt.app.Config.API_BASE_URL
import tv.trakt.trakt.app.core.streamings.data.local.StreamingLocalDataSource
import tv.trakt.trakt.app.core.streamings.data.local.StreamingStorage
import tv.trakt.trakt.app.core.streamings.data.remote.StreamingApiClient
import tv.trakt.trakt.app.core.streamings.data.remote.StreamingRemoteDataSource
import tv.trakt.trakt.app.core.streamings.utilities.PriorityStreamingServiceProvider

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
