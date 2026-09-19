package tv.trakt.trakt.app.core.streamings.di

import android.annotation.SuppressLint
import android.provider.Settings
import androidx.lifecycle.SavedStateHandle
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.app.core.plex.PlexStreamApi
import tv.trakt.trakt.app.core.plex.data.PlexApiClient
import tv.trakt.trakt.app.core.plex.data.PlexRemoteDataSource
import tv.trakt.trakt.app.core.plex.data.PlexTimelineClient
import tv.trakt.trakt.app.core.streamings.AllStreamingsViewModel
import tv.trakt.trakt.common.Config.API_BASE_URL

@SuppressLint("HardwareIds")
internal val plexDataModule = module {
    single {
        PlexTimelineClient(
            httpClientEngine = get(),
            // Stable per device + signing key, survives reinstall. Plex only needs it to tell sessions apart.
            clientIdentifier = Settings.Secure
                .getString(androidContext().contentResolver, Settings.Secure.ANDROID_ID)
                .orEmpty(),
        )
    }

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
