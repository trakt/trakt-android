package tv.trakt.trakt.app.core.streamings.di

import androidx.lifecycle.SavedStateHandle
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.app.core.plex.PlexStreamApi
import tv.trakt.trakt.app.core.plex.data.PlexApiClient
import tv.trakt.trakt.app.core.plex.data.PlexRemoteDataSource
import tv.trakt.trakt.app.core.streamings.AllStreamingsViewModel
import tv.trakt.trakt.common.Config.API_BASE_URL

internal val plexDataModule = module {
    single<PlexRemoteDataSource> {
        PlexApiClient(
            api = PlexStreamApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = get(),
                httpClientConfig = get(named("authorizedClientConfig")),
            ),
        )
    }
}

internal val allStreamingsModule = module {
    viewModel { (stateHandle: SavedStateHandle) ->
        AllStreamingsViewModel(
            savedStateHandle = stateHandle,
            sessionManager = get(),
            getAllStreamingsUseCase = get(),
        )
    }
}
