package tv.trakt.trakt.app.core.streamings.di

import androidx.lifecycle.SavedStateHandle
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.WatchnowApi
import tv.trakt.trakt.app.Config.API_BASE_URL
import tv.trakt.trakt.app.core.streamings.AllStreamingsViewModel
import tv.trakt.trakt.app.core.streamings.data.local.StreamingLocalDataSource
import tv.trakt.trakt.app.core.streamings.data.local.StreamingStorage
import tv.trakt.trakt.app.core.streamings.data.remote.StreamingApiClient
import tv.trakt.trakt.app.core.streamings.data.remote.StreamingRemoteDataSource
import tv.trakt.trakt.app.core.streamings.usecase.GetAllStreamingsUseCase
import tv.trakt.trakt.app.core.streamings.utilities.PriorityStreamingServiceProvider

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

    single<PriorityStreamingServiceProvider> {
        PriorityStreamingServiceProvider()
    }

    single<StreamingLocalDataSource> {
        StreamingStorage()
    }
}

internal val streamingsModule = module {
    factory {
        GetAllStreamingsUseCase(
            remoteStreamingSource = get(),
            remoteShowSource = get(),
            remoteMovieSource = get(),
            remoteEpisodeSource = get(),
            localStreamingSource = get(),
        )
    }

    viewModel { (stateHandle: SavedStateHandle) ->
        AllStreamingsViewModel(
            savedStateHandle = stateHandle,
            sessionManager = get(),
            getAllStreamingsUseCase = get(),
        )
    }
}
