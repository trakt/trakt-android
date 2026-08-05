package tv.trakt.trakt.core.streamings.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.WatchnowApi
import tv.trakt.trakt.common.Config.API_BASE_URL
import tv.trakt.trakt.common.core.streamings.data.local.StreamingLocalDataSource
import tv.trakt.trakt.common.core.streamings.data.local.StreamingStorage
import tv.trakt.trakt.common.core.streamings.data.remote.StreamingApiClient
import tv.trakt.trakt.common.core.streamings.data.remote.StreamingRemoteDataSource
import tv.trakt.trakt.common.core.streamings.helpers.PriorityStreamingServiceProvider
import tv.trakt.trakt.core.streamings.AllStreamingsViewModel
import tv.trakt.trakt.core.streamings.usecase.GetAllStreamingsUseCase

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

internal val streamingsModule = module {
    factoryOf(::GetAllStreamingsUseCase)
    viewModelOf(::AllStreamingsViewModel)
}
